package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.external.OIDCPublicKeysResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "googleAuthClient",
        url = "https://www.googleapis.com"
)
public interface GoogleOauthClient {
    @GetMapping("/oauth2/v3/certs")
    OIDCPublicKeysResponse getGoogleOIDCOpenKeys();
}
