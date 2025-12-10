package com.epik.domain.oauth.service;

import com.epik.domain.oauth.client.KakaoOauthClient;
import com.epik.domain.oauth.dto.OIDCDecodePayload;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.external.OIDCPublicKeysResponse;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.OidcAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.epik.domain.oauth.dto.SocialProvider.KAKAO;

/**
 * 카카오 OIDC 인증 서비스
 * AbstractOidcService를 상속받아 카카오 특화 로직만 구현
 */
@Slf4j
@Service
public class KakaoOidcService extends AbstractOidcService {

    private final KakaoOauthClient kakaoOauthClient;
    private final String iss;
    private final String appId;

    public KakaoOidcService(
            KakaoOauthClient kakaoOauthClient,
            ObjectMapper objectMapper,
            @Value("${oauth.kakao.iss}") String iss,
            @Value("${oauth.kakao.app-id}") String appId) {
        super(objectMapper);
        this.kakaoOauthClient = kakaoOauthClient;
        this.iss = iss;
        this.appId = appId;

        log.info("🚀 KakaoOidcService 초기화 완료 - ISS: {}, APP-ID: {}", iss, appId);
    }

    @Override
    protected OIDCPublicKeysResponse fetchPublicKeys() {
        log.debug("🌐 카카오 OIDC 공개키 목록 조회 시작");

        try {
            OIDCPublicKeysResponse response = kakaoOauthClient.getKakaoOIDCOpenKeys();
            log.debug("✅ 공개키 목록 조회 성공: {} 개의 키", response.getKeys().size());
            return response;
        } catch (FeignException e) {
            log.error("❌ 카카오 공개키 조회 실패", e);
            throw new OidcAuthenticationException(ErrorCode.OIDC_SERVER_ERROR);
        }
    }

    @Override
    protected void validateAdditionalClaims(Claims claims) {
        // 카카오는 audience를 단일 문자열로 제공
        Set<String> audienceSet = claims.getAudience();
        if (audienceSet.isEmpty()) {
            log.error("❌ Audience 클레임이 비어있음");
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        String audience = audienceSet.iterator().next();
        if (!appId.equals(audience)) {
            log.error("❌ Audience 불일치 - 예상: {}, 실제: {}", appId, audience);
            throw new OidcAuthenticationException(ErrorCode.INVALID_OR_EXPIRED_TOKEN);
        }

        log.debug("✅ Audience 검증 성공: {}", audience);
    }

    @Override
    protected OIDCDecodePayload buildPayload(Claims claims) {
        Set<String> audienceSet = claims.getAudience();
        String audience = audienceSet.iterator().next();

        return new OIDCDecodePayload(
                claims.getIssuer(),
                audience,
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("nickname", String.class)  // 카카오는 nickname 사용
        );
    }

    @Override
    protected String getIssuer() {
        return iss;
    }

    @Override
    protected SocialProvider getProviderName() {
        return KAKAO;
    }
}