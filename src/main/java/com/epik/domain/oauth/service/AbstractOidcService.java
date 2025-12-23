package com.epik.domain.oauth.service;

import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.external.OIDCPublicKey;
import com.epik.domain.oauth.dto.external.OIDCPublicKeysResponse;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.OidcAuthenticationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOidcService implements SocialAuthProvider {

    protected final ObjectMapper objectMapper;
    private static final int JWT_PARTS_COUNT = 3;

    @Override
    public SocialUserInfo getUserInfo(String token) {
        return getOIDCDecodePayload(token);
    }

    /**
     * OIDC ID Token을 검증하고 페이로드를 반환하는 템플릿 메서드
     */
    public SocialUserInfo getOIDCDecodePayload(String token) {
        // 1. JWT 헤더에서 kid 추출
        String kid = extractKidFromToken(token);

        // 2. OIDC Provider로부터 공개키 목록 조회
        OIDCPublicKeysResponse publicKeysResponse = fetchPublicKeys();

        // 3. kid와 매칭되는 공개키 찾기
        OIDCPublicKey oidcPublicKey = findPublicKeyByKid(kid, publicKeysResponse);

        // 4. RSA 공개키 생성
        PublicKey publicKey = generateRSAPublicKey(oidcPublicKey.getN(), oidcPublicKey.getE());

        // 5. JWT 토큰 검증 및 Claims 추출
        Claims claims = verifyTokenAndExtractClaims(token, publicKey);

        // 6. Claims를 OIDCDecodePayload로 변환
        return buildPayload(claims);
    }

    /**
     * JWT 헤더에서 kid(Key ID) 추출
     */
    protected String extractKidFromToken(String token) {
        log.debug("🔍 JWT 헤더에서 KID 추출 시작");

        if (token == null || token.trim().isEmpty()) {
            log.error("❌ 토큰이 null이거나 비어있음");
            throw new OidcAuthenticationException(ErrorCode.MALFORMED_ID_TOKEN);
        }

        try {
            String[] parts = token.split("\\.");
            log.debug("📝 토큰 분리 결과: {} 개 파트", parts.length);

            if (parts.length != JWT_PARTS_COUNT) {
                log.error("❌ 잘못된 JWT 형식 - 파트 수: {} (예상: {})", parts.length, JWT_PARTS_COUNT);
                throw new OidcAuthenticationException(ErrorCode.MALFORMED_ID_TOKEN);
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            log.debug("📝 디코딩된 헤더 JSON: {}", headerJson);

            JsonNode headerNode = objectMapper.readTree(headerJson);

            if (!headerNode.has("kid")) {
                log.error("❌ 헤더에 kid 필드가 없습니다");
                throw new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
            }

            String kid = headerNode.get("kid").asText();
            log.debug("✅ KID 추출 성공: '{}'", kid);

            return kid;

        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ JWT 헤더에서 KID 추출 실패", e);
            throw new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
        }
    }

    /**
     * kid와 매칭되는 공개키 찾기
     */
    protected OIDCPublicKey findPublicKeyByKid(String kid, OIDCPublicKeysResponse publicKeysResponse) {
        log.debug("🔍 KID로 공개키 찾기 시작: '{}'", kid);

        return publicKeysResponse.getKeys().stream()
                .filter(key -> key.getKid().equals(kid))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("❌ 일치하는 KID를 찾을 수 없음: '{}'", kid);
                    return new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
                });
    }

    /**
     * RSA 공개키 생성
     */
    protected PublicKey generateRSAPublicKey(String modulus, String exponent) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
            byte[] decodeE = Base64.getUrlDecoder().decode(exponent);

            BigInteger n = new BigInteger(1, decodeN);
            BigInteger e = new BigInteger(1, decodeE);

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            log.debug("✅ RSA 공개키 생성 완료");
            return publicKey;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            log.error("❌ RSA 공개키 생성 실패: {}", e.getMessage(), e);
            throw new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
        }
    }

    /**
     * JWT 토큰 검증 및 Claims 추출
     * Provider별로 검증 로직이 다를 수 있으므로 추상 메서드로 정의
     */
    protected Claims verifyTokenAndExtractClaims(String token, PublicKey publicKey) {
        log.debug("🔐 JWT 토큰 검증 시작");

        try {
            Jws<Claims> jws = parseAndVerifyToken(token, publicKey);
            Claims claims = jws.getPayload();

            // Provider별 추가 검증 (aud 등)
            validateAdditionalClaims(claims);

            log.debug("✅ JWT 토큰 검증 완료");
            return claims;

        } catch (ExpiredJwtException e) {
            log.warn("❌ ID Token expired: exp={}", e.getClaims().getExpiration());
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        } catch (IncorrectClaimException e) {
            log.warn("❌ Invalid claim: {}", e.getMessage());
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ ID Token parsing/validation failed", e);
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }
    }

    /**
     * JWT 파싱 및 기본 검증 (서명, issuer)
     */
    protected Jws<Claims> parseAndVerifyToken(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    // ========== 하위 클래스에서 구현해야 하는 추상 메서드 ==========

    /**
     * OIDC Provider로부터 공개키 목록 조회
     * Provider별로 엔드포인트가 다르므로 하위 클래스에서 구현
     */
    protected abstract OIDCPublicKeysResponse fetchPublicKeys();

    /**
     * Claims 추가 검증 (예: audience 검증)
     * Provider별로 검증 방식이 다를 수 있으므로 하위 클래스에서 구현
     */
    protected abstract void validateAdditionalClaims(Claims claims);

    /**
     * Claims를 OIDCDecodePayload로 변환
     * Provider별로 claim 이름이 다를 수 있으므로 하위 클래스에서 구현
     */
    protected abstract SocialUserInfo buildPayload(Claims claims);

    /**
     * OIDC Provider의 issuer 반환
     */
    protected abstract String getIssuer();


}
