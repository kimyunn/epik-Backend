package com.epik.domain.auth.service;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.jwt.JwtTokenProvider;
import com.epik.domain.auth.token.RefreshTokenService;
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

    /**
     * Access Token과 Refresh Token을 생성하고,
     * Refresh Token 저장 로직까지 처리한 뒤 최종 응답 DTO(TokenResponse)를 반환한다.
     *
     * 처리 과정:
     * 1) Access Token 생성
     * 2) Refresh Token 생성
     * 3) Refresh Token에서 만료 시간 추출
     * 4) Refresh Token 저장 또는 갱신
     * 5) 두 토큰을 포함한 TokenResponse 생성
     *
     * 해당 메서드는 로그인 및 인증 이후 토큰 발급과 관련된
     * 모든 절차를 하나의 흐름으로 캡슐화하여 제공한다.
     *
     * @param user 토큰을 발급할 사용자
     * @return Access Token + Refresh Token을 포함한 TokenResponse
     */
    public TokenResponse createTokenResponseFrom(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        LocalDateTime expiryAt = jwtTokenProvider.extractExpiry(refreshToken);
        refreshTokenService.saveOrUpdate(user, refreshToken, expiryAt);

        return new TokenResponse(accessToken, refreshToken);
    }

}
