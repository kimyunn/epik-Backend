package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.external.JwksResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "GoogleJwksClient",
        url = "https://www.googleapis.com"
)
public interface GoogleJwksClient {
    @GetMapping("/oauth2/v3/certs")
    JwksResponse getJwks();
}
