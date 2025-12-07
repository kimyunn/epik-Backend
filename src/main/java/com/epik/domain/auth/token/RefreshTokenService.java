package com.epik.domain.auth.token;

import com.epik.domain.auth.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Refresh Token 저장 또는 갱신
     *
     * 사용자별로 하나의 Refresh Token만 유지하는 정책에 따라,
     * 기존에 저장된 Refresh Token이 존재하면 새로운 토큰 값과 만료 시간으로 갱신하고,
     * 존재하지 않으면 새롭게 저장한다.
     *
     * @param user     토큰을 소유한 사용자
     * @param token    새로 생성된 Refresh Token 문자열
     * @param expiryAt Refresh Token의 만료 시각
     */
    @Transactional
    public void saveOrUpdate(User user, String token, LocalDateTime expiryAt) {

        // 사용자별로 저장된 Refresh Token 조회
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByUserId(user.getId());

        // 존재한다면 -> 토큰 값과 만료 시간을 업데이트
        if (savedToken.isPresent()) {
            RefreshToken refreshToken = savedToken.get();
            refreshToken.update(token, expiryAt);

        } else {
            // 존재하지 않는다면 -> 새 엔티티 생성 후 저장
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .token(token)
                            .user(user)
                            .expiresAt(expiryAt)
                            .build()
            );
        }
    }
}
