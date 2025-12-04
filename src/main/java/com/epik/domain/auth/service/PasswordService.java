package com.epik.domain.auth.service;

import com.epik.domain.auth.entity.PasswordResetToken;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.repository.PasswordResetTokenRepository;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.global.exception.BusinessException;
import com.epik.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public void sendPasswordResetEmail(String email) {

        log.info("[PasswordReset] 요청 수신 - email={}", email);

        // 1. 이메일 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[PasswordReset] 존재하지 않는 이메일 요청 - email={}", email);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("[PasswordReset] 사용자 조회 성공 - userId={}, email={}", user.getId(), email);

        // 2. 기존 미사용 토큰 무효화
        passwordResetTokenRepository.findByUserAndUsedFalse(user)
                .ifPresent(oldToken -> {
                    oldToken.use();
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
}