package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.external.JwksResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "KakaoJwksClient",
        url = "https://kauth.kakao.com"
)
public interface KakaoJwksClient {
    @GetMapping("/.well-known/jwks.json")
    JwksResponse getJwks();
}
