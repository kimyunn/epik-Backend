package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.external.NaverUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "naverOauthClient",
        url = "https://openapi.naver.com"
)
public interface NaverOauthClient {
    @GetMapping("/v1/nid/me")
    NaverUserInfoResponse getUserInfo(@RequestHeader("Authorization") String authorization);

}
