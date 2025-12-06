package com.epik.domain.auth.jwt;

import com.epik.domain.auth.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
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
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
                            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration) {
        // 설정파일에 있는 base64로 인코딩된 문자열을 가져와서 -> 디코딩
        byte[] decodedKey = Decoders.BASE64.decode(secretKey);
        // -> key 객체 -> 그래야 jwt 서명에 사용 가능
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     * @param id   사용자 식별자
     * @param role 사용자 권한
     * @return 생성된 Access Token(JWT 문자열)
     */
    public String createAccessToken(Long id, UserRole role) {
        /**
         * 사용자의 식별자(userId)와 권한(role)을 기반으로 Access Token을 생성한다.
         * Access Token은 짧은 만료 시간을 가지며 클라이언트의 인증 요청 시 사용된다.
         *
         * 포함되는 정보:
         * - subject: userId
         * - role: 사용자 권한
         * - iat: 발급 시간(issuedAt)
         * - exp: 만료 시간(expiration)
         *
         * JWT는 서버의 SecretKey로 서명되어 위변조 여부를 검증할 수 있다.
         */

        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * @param id   사용자 식별자
     * @param role 사용자 권한
     * @return 생성된 Refresh Token(JWT 문자열)
     */
    public String createRefreshToken(Long id, UserRole role) {
        /**
         * 사용자의 식별자(userId)와 권한(role)을 기반으로 Refresh Token을 생성한다.
         * Refresh Token은 Access Token보다 훨씬 긴 만료 시간을 가지며,
         * 만료된 Access Token을 재발급할 때 검증용으로 사용된다.
         *
         * 포함되는 정보:
         * - subject: userId
         * - role: 사용자 권한
         * - iat: 발급 시간(issuedAt)
         * - exp: 만료 시간(expiration)
         *
         * Access Token과 동일한 구조의 JWT이지만,
         * 인증 요청에 직접 사용되지 않고 오로지 재발급 용도로만 사용된다.
         */

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
     * Refresh Token의 만료 시간을 추출하여 LocalDateTime으로 변환한다.
     *
     * Refresh Token 내부의 exp(만료 시각)는 Date 타입으로 저장되므로,
     * DB에 저장하기 위해 LocalDateTime 타입으로 변환해 반환한다.
     *
     * @param refreshToken 만료 시간을 확인할 Refresh Token(JWT)
     * @return LocalDateTime 형태의 만료 시각
     */
    public LocalDateTime extractExpiry(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        return claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * JWT를 파싱하여 Claims(페이로드 데이터)를 추출한다.
     *
     * - SecretKey를 사용하여 JWT의 Signature를 검증한다.
     * - 서명이 유효하고 토큰 구조가 올바른 경우 Claims(Payload)를 반환한다.
     * - 위변조된 토큰이거나 형식이 잘못된 경우 예외가 발생한다.
     *
     * @param token 파싱하려는 JWT 문자열
     * @return Claims 객체(Payload 정보)
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
