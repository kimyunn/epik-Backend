package com.epik.domain.oauth.client;

import com.epik.domain.oauth.dto.response.NaverUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "naverOauthClient",
        url = "https://openapi.naver.com"
)
public interface NaverOauthClient {

    @GetMapping
    NaverUserInfoResponse getUserInfo(@RequestHeader("Authorization") String authorization);

}
