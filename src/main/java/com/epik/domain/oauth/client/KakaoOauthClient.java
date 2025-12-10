package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.external.OIDCPublicKeysResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "kakaoAuthClient", // FeignClient 빈(Bean)의 식별자
        url = "https://kauth.kakao.com" // 요청을 보낼 카카오 API 기본 주소
)
public interface KakaoOauthClient {
//    @Cacheable(cacheNames = "KakaoOICD", cacheManager = "oidcCacheManager")
    @GetMapping("/.well-known/jwks.json") // 이 메서드는 해당 경로로 GET 요청 보냄
    OIDCPublicKeysResponse getKakaoOIDCOpenKeys();
}
