package com.epik.domain.auth.service;

import com.epik.domain.auth.entity.PasswordResetToken;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.repository.PasswordResetTokenRepository;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.global.exception.custom.BusinessException;
import com.epik.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 이메일을 발송한다.
     * 미등록 이메일인 경우 User Enumeration 공격 방지를 위해 조용히 무시한다.
     *
     * @param email 비밀번호를 재설정할 이메일
     */
    public void sendPasswordResetEmail(String email) {

        log.info("[PasswordReset] 요청 수신 - email={}", email);

        // 1. 이메일 존재 여부 확인
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.info("[PasswordReset] 미등록 이메일 요청 (조용히 무시) - email={}", email);
            return;  // 예외 던지지 않음!
        }

        User user = userOptional.get();

        log.info("[PasswordReset] 사용자 조회 성공 - userId={}, email={}", user.getId(), email);

        // 2. 기존 미사용 토큰 무효화
        passwordResetTokenRepository.findByUserAndUsedFalse(user)
                .ifPresent(oldToken -> {
                    oldToken.markAsUsed();
                    passwordResetTokenRepository.save(oldToken);
                    log.info("[PasswordReset] 기존 토큰 무효화 - token={}, userId={}",
                            oldToken.getToken(), user.getId());
                });

        // 3. 새 토큰 생성
        PasswordResetToken passwordResetToken = PasswordResetToken.create(user);

        log.info("[PasswordReset] 새 토큰 생성 - token={}, userId={}, expiresAt={}",
                passwordResetToken.getToken(),
                user.getId(),
                passwordResetToken.getExpiresAt());

        // 4. 토큰 저장
        passwordResetTokenRepository.save(passwordResetToken);
        log.info("[PasswordReset] 새 토큰 저장 완료 - tokenId={}, userId={}",
                passwordResetToken.getId(),
                user.getId());

        // 5. 이메일 전송 시도
        try {
            emailService.sendPasswordResetEmail(email, passwordResetToken.getToken());
            log.info("[PasswordReset] 이메일 발송 성공 - email={}, token={}", email, passwordResetToken.getToken());
        } catch (Exception e) {
            log.error("[PasswordReset] 이메일 발송 실패 - email={}, token={}, 원인={}",
                    email, passwordResetToken.getToken(), e.getMessage(), e);
        }
    }

    /**
     * 토큰을 검증하고 비밀번호를 재설정한다.
     *
     * @param token 비밀번호 재설정 토큰
     * @param newPassword 새 비밀번호
     * @throws BusinessException 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("[비밀번호 재설정] 시작");

        // 1. 토큰 조회 및 검증
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> {
                    log.warn("[비밀번호 재설정] 실패 - 유효하지 않은 토큰");
                    return new BusinessException(ErrorCode.INVALID_TOKEN);
                });

        // 2. 토큰 유효성 검증 (Entity에서 예외 발생)
        passwordResetToken.validate();

        // 3. 토큰 폐기
        passwordResetToken.markAsUsed();

        // 4. 사용자 조회
        User user = passwordResetToken.getUser();
        log.info("[비밀번호 재설정] 사용자 확인 - email: {}", user.getEmail());

        // 5. 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);

        log.info("[비밀번호 재설정] 완료 - email: {}", user.getEmail());
    }
}