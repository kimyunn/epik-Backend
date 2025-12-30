package com.epik.domain.oauth.controller;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.oauth.dto.request.SocialLoginRequest;
import com.epik.domain.oauth.dto.request.SocialSignupRequest;
import com.epik.domain.oauth.dto.response.SocialCheckResponse;
import com.epik.domain.oauth.service.SocialAuthService;
import com.epik.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/social/login/{provider}")
    public ResponseEntity<ApiResponse<SocialCheckResponse>> login(
            @PathVariable String provider,
            @RequestBody SocialLoginRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        socialAuthService.handleSocialLogin(provider, request.getToken())
                )
        );
    }

    @PostMapping("/social/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> socialSignup(@RequestBody @Valid SocialSignupRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        socialAuthService.completeSocialSignup(request)
                )
        );
    }

}
