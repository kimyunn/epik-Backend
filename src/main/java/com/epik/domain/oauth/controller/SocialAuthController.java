package com.epik.domain.oauth.controller;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.request.SocialLoginRequest;
import com.epik.domain.oauth.dto.request.SocialSignupRequest;
import com.epik.domain.oauth.dto.response.SocialCheckResponse;
import com.epik.domain.oauth.service.AbstractOidcService;
import com.epik.domain.oauth.service.OidcServiceFactory;
import com.epik.domain.oauth.service.SocialAuthProvider;
import com.epik.domain.oauth.service.SocialAuthService;
import com.epik.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final OidcServiceFactory oidcServiceFactory;
    private final SocialAuthService socialAuthService;

    @PostMapping("/social/login/{provider}")
    public ResponseEntity<ApiResponse<SocialCheckResponse>> login(
            @PathVariable SocialProvider provider,
            @RequestBody SocialLoginRequest request) {

        SocialAuthProvider socialAuthProvider = oidcServiceFactory.getOidcService(provider);
        SocialUserInfo payload = socialAuthProvider.getUserInfo(request.getToken());
        SocialCheckResponse response = socialAuthService.handleSocialLogin(
                provider,
                payload.getSub(),
                payload.getEmail()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/social/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> socialSignup(@RequestBody SocialSignupRequest request) {
        // 여기서는 register token
        // nickname, termsAggred값이 넘어와야함.
        // 그래서 회원 가입 정보 검증하고
        // 디비에 회원 정보 저장하고
        // 토큰 생성해서 발급
        TokenResponse response = socialAuthService.completeSocialSignup(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
