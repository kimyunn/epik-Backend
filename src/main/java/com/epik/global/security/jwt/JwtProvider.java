package com.epik.global.security.jwt;

import com.epik.domain.auth.entity.enums.UserRole;
import com.epik.domain.oauth.dto.enums.SocialProvider;
import com.epik.global.exception.custom.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final long registerTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
                       @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration,
                       @Value("${jwt.register-token.expiration}") long registerTokenExpiration) {

        // Base64 디코딩 후 HMAC SecretKey 생성
        byte[] byteSecretKey = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(byteSecretKey);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.registerTokenExpiration = registerTokenExpiration;
    }

    /**
     * Access Token을 생성한다.
     *
     * @param id 사용자 식별자
     * @param role 사용자 권한
     * @return JWT 형식의 Access Token
     */
    public String createAccessToken(Long id, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token을 생성한다.
     *
     * @param id 사용자 식별자
     * @param role 사용자 권한
     * @return JWT 형식의 Refresh Token
     */
    public String createRefreshToken(Long id, UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT에서 만료 시간을 추출하여 LocalDateTime으로 변환한다.
     *
     * @param refreshToken 만료 시간을 확인할 Refresh Token
     * @return 만료 시간 (LocalDateTime)
     */
    public LocalDateTime extractExpiry(String refreshToken) {
        Claims claims = validateAndGetClaims(refreshToken);

        return claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * JWT를 검증하고 Claims를 추출한다.
     * 서명, 구조, 만료 시간 등을 자동으로 검증한다.
     *
     * @param token JWT 토큰
     * @return 검증된 Claims 객체
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Token의 유효성을 검증
     *
     * @param token 검증할 Token
     * @throws BusinessException 토큰이 null, 공백이거나 유효하지 않은 경우
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);  // Claims는 버림
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Register Token을 생성한다.
     * @param providerName 카카오/구글 등 소셜 플랫폼 이름
     * @param socialId 소셜 로그인 계정의 고유 ID (sub)
     * @param email 소셜 계정에서 가져온 이메일
     * @return Register Token
     */
    public String createRegisterToken(SocialProvider providerName, String socialId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + registerTokenExpiration);

        return Jwts.builder()
                .subject("social_signup")
                .claim("provider", providerName)
                .claim("socialId", socialId)
                .claim("email", email)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }


    public Long getUserId(String token) {
        Claims claims = validateAndGetClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.get("role", String.class);
    }
}
