package com.epik.domain.oauth.service;

import com.epik.domain.oauth.client.GoogleOauthClient;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.external.OIDCPublicKeysResponse;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.OidcAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.epik.domain.oauth.dto.SocialProvider.GOOGLE;

/**
 * 구글 OIDC 인증 서비스
 * AbstractOidcService를 상속받아 구글 특화 로직만 구현
 */
@Slf4j
@Service
public class GoogleOidcService extends AbstractOidcService {

    private final GoogleOauthClient googleOauthClient;
    private final String iss;
    private final List<String> googleClientIds;

    public GoogleOidcService(
            GoogleOauthClient googleOauthClient,
            ObjectMapper objectMapper,
            @Value("${oauth.google.iss}") String iss,
            @Value("${oauth.google.client-ids}") List<String> googleClientIds) {
        super(objectMapper);
        this.googleOauthClient = googleOauthClient;
        this.iss = iss;
        this.googleClientIds = googleClientIds;

        log.info("🚀 GoogleOidcService 초기화 완료 - ISS: {}, 클라이언트 IDs: {}", iss, googleClientIds);
    }

    @Override
    protected OIDCPublicKeysResponse fetchPublicKeys() {
        log.debug("🌐 Google OIDC 공개키 목록 조회 시작");

        try {
            OIDCPublicKeysResponse response = googleOauthClient.getGoogleOIDCOpenKeys();
            log.debug("✅ 공개키 목록 조회 성공: {} 개의 키", response.getKeys().size());
            return response;
        } catch (FeignException e) {
            log.error("❌ Google 공개키 조회 실패", e);
            throw new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
        }
    }

    @Override
    protected void validateAdditionalClaims(Claims claims) {
        // 구글은 audience를 배열 또는 단일 문자열로 제공할 수 있음
        Object audObj = claims.get("aud");

        log.debug("📝 AUD 검증 시작 - 타입: {}, 값: {}",
                audObj != null ? audObj.getClass().getName() : "null", audObj);

        if (audObj == null) {
            log.error("❌ Audience 클레임이 없음");
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        boolean isValid = false;

        // AUD가 String인 경우
        if (audObj instanceof String tokenAud) {
            isValid = googleClientIds.contains(tokenAud);
            log.debug("📝 String AUD 검증: '{}' - 결과: {}", tokenAud, isValid);
        }
        // AUD가 Collection인 경우 (구글 스타일)
        else if (audObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> audCollection = (Collection<String>) audObj;
            isValid = googleClientIds.stream()
                    .anyMatch(audCollection::contains);
            log.debug("📝 Collection AUD 검증: {} - 결과: {}", audCollection, isValid);
        }
        // 예상하지 못한 타입
        else {
            log.error("❌ 예상하지 못한 AUD 타입: {}", audObj.getClass().getName());
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        if (!isValid) {
            log.error("❌ Audience 검증 실패 - 허용된 클라이언트 IDs: {}", googleClientIds);
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        log.debug("✅ Audience 검증 성공");
    }

    @Override
    protected SocialUserInfo buildPayload(Claims claims) {
        String audience = extractFirstAudience(claims);

        return new SocialUserInfo(
                claims.getIssuer(),
                audience,
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("name", String.class)  // 구글은 name 사용
        );
    }

    /**
     * Google JWT의 aud 클레임에서 첫 번째 값을 안전하게 추출
     */
    private String extractFirstAudience(Claims claims) {
        Object audObj = claims.get("aud");

        if (audObj instanceof String) {
            return (String) audObj;
        }

        if (audObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> audCollection = (Collection<String>) audObj;
            if (!audCollection.isEmpty()) {
                String firstAud = audCollection.iterator().next();
                log.debug("📝 배열에서 첫 번째 AUD 추출: '{}'", firstAud);
                return firstAud;
            }
        }

        log.error("❌ AUD 클레임에서 유효한 값을 추출할 수 없음: {}", audObj);
        throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
    }

    @Override
    protected String getIssuer() {
        return iss;
    }

    @Override
    public SocialProvider getProviderName() {
        return GOOGLE;
    }
}

