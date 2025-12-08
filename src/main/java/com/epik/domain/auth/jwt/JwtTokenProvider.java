package com.epik.domain.auth.jwt;

import com.epik.domain.auth.entity.enums.UserRole;
import com.epik.global.exception.BusinessException;
import com.epik.global.exception.ErrorCode;
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
                .claim("role", role)
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
     * Refresh Token의 유효성을 검증한다.
     *
     * @param token 검증할 Refresh Token
     * @throws BusinessException 토큰이 null, 공백이거나 유효하지 않은 경우
     */
    public void validateRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        validateAndGetClaims(token); // 예외 자동 전달
    }

}
