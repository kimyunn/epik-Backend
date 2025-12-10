package com.epik.domain.oauth.controller;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.oauth.dto.OIDCDecodePayload;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.request.OIDCLoginRequest;
import com.epik.domain.oauth.dto.request.SocialSignupRequest;
import com.epik.domain.oauth.dto.response.SocialCheckResponse;
import com.epik.domain.oauth.service.AbstractOidcService;
import com.epik.domain.oauth.service.OidcServiceFactory;
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
            @RequestBody OIDCLoginRequest request) {

        // 1) URL Path로 전달된 provider 값을 기반으로
        //    해당 소셜 플랫폼(Kakao, Google 등)의 OIDC 서비스 구현체를 조회한다.
        //    예) /social/login/kakao → KakaoOidcService 반환
        AbstractOidcService oidcService = oidcServiceFactory.getOidcService(provider);

        // 2) 전달받은 ID Token을 검증하고,
        //    토큰 내 Claims에서 사용자 식별 정보(sub, email 등)를 추출한다.
        OIDCDecodePayload payload = oidcService.getOIDCDecodePayload(request.getToken());

        // 3) 추출된 사용자 정보를 기반으로
        //    - 기존 회원이면 AccessToken + RefreshToken 발급
        //    - 신규 회원이면 회원가입을 위한 Register Token 발급
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
