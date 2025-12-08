package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.jwt.JwtTokenProvider;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.domain.auth.token.RefreshToken;
import com.epik.domain.auth.token.RefreshTokenRepository;
import com.epik.domain.auth.token.RefreshTokenService;
import com.epik.global.exception.BusinessException;
import com.epik.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Access Token과 Refresh Token을 생성하고 응답 DTO로 반환한다.
     *
     * @param user 토큰을 발급할 사용자
     * @return Access Token과 Refresh Token을 포함한 응답 객체
     */
    public TokenResponse createTokenResponseFrom(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        LocalDateTime expiryAt = jwtTokenProvider.extractExpiry(refreshToken);
        refreshTokenService.saveOrUpdate(user, refreshToken, expiryAt);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * Refresh Token을 검증하고 새로운 토큰 쌍을 재발급한다.
     *
     * @param refreshToken 재발급 요청에 사용된 Refresh Token
     * @return 새로 생성된 Access Token과 Refresh Token
     * @throws BusinessException 토큰이 유효하지 않거나 DB에 존재하지 않는 경우
     */
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // refresh token 유효성 검증 후 userId 추출
        Claims claims = jwtTokenProvider.validateAndGetClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        // DB 검증
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("리프레시 토큰 미존재: userId={}", userId);
                    return new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });

        // DB 값과 요청 값 일치 여부 확인
        validateRefreshTokenValue(refreshToken, storedToken);

        // 유저 조회 (role 최신화)
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());

        // 기존 refreshToken 무효화 + 새 토큰 저장
        LocalDateTime expiryAt = jwtTokenProvider.extractExpiry(newRefreshToken);
        refreshTokenService.saveOrUpdate(user, refreshToken, expiryAt);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 요청된 Refresh Token과 DB에 저장된 토큰의 일치 여부를 검증한다.
     *
     * @param refreshToken 요청으로 전달된 토큰
     * @param storedToken DB에 저장된 토큰 엔티티
     * @throws BusinessException 토큰이 일치하지 않는 경우
     */
    private void validateRefreshTokenValue(String refreshToken, RefreshToken storedToken) {
        if (!storedToken.getToken().equals(refreshToken)) {
            log.warn("[Security] RefreshToken mismatch - userId={}", storedToken.getUser().getId());
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

}
