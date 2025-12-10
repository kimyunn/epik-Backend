package com.epik.domain.oauth.service;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.domain.auth.service.TokenService;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.SocialRegisterPayload;
import com.epik.domain.oauth.dto.request.SocialSignupRequest;
import com.epik.domain.oauth.dto.response.SocialCheckResponse;
import com.epik.domain.oauth.entity.SocialLogin;
import com.epik.domain.oauth.entity.SocialLoginProvider;
import com.epik.domain.oauth.repository.SocialLoginProviderRepository;
import com.epik.domain.oauth.repository.SocialLoginRepository;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.BusinessException;
import com.epik.global.exception.custom.OidcAuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialLoginRepository socialLoginRepository;
    private final SocialLoginProviderRepository socialLoginProviderRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;


    public SocialCheckResponse handleSocialLogin(SocialProvider providerName, String socialId, String email) {

        // 여기서 해야할 일은
        // 1. 기존 회원인지 체크하기
        // 2. 기존 회원이면 엑세스/리프레시 토큰 발급
        // 3. 신규 회원이면 레지스터 토큰 발급

        // 일단 기존 회원인지 체크하기
        Optional<SocialLogin> existingSocialLogin = socialLoginRepository.findBySocialIdAndProviderName(socialId, providerName);

        if (existingSocialLogin.isPresent()) {
            SocialLogin socialLogin = existingSocialLogin.get();
            User user = userRepository.findById(socialLogin.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            TokenResponse tokenResponseFrom = tokenService.createTokenResponseFrom(user);

            return SocialCheckResponse.loginSuccess(
                    tokenResponseFrom.getAccessToken(),
                    tokenResponseFrom.getRefreshToken()
            );
        } else {
            // 신규 회원 처리
            // 레지스터 토큰 발급
            return SocialCheckResponse.needSignup(
                    tokenService.createRegisterToken(providerName, socialId, email)
            );
        }
    }

    public TokenResponse completeSocialSignup(SocialSignupRequest request) {
        // Request : 닉네임, 필수 약관 동의 여부
        // 토큰 검증(유효성 + 만료 체크)
//        tokenService.validateRegisterTokenExpired(request.getRegisterToken());

        // 유효한 토큰에서 사용자 정보 추출
        // 아 저장하려면 토큰에서 회원 정보를 꺼내야하잖어?
        SocialRegisterPayload socialRegisterPayload = tokenService.decodeRegisterToken(request.getRegisterToken());

        // DB 저장
        // 회원 테이블 -> 저장
        User socialUser = User.createSocialUser(socialRegisterPayload.getEmail(), socialRegisterPayload.getNickname());
        User savedUser = userRepository.save(socialUser);

        // provider 엔티티 조회
        SocialLoginProvider provider = socialLoginProviderRepository
                .findByProviderName(socialRegisterPayload.getProvider())
                .orElseThrow(() -> new OidcAuthenticationException(ErrorCode.PROVIDER_NOT_FOUND));

        // 소셜 로그인 테이블 -> 저장
        socialLoginRepository.save(
                SocialLogin.builder()
                        .user(savedUser)
                        .provider(provider) // provider을 넣어야하는데 연관관계야
                        .socialId(socialRegisterPayload.getSocialId())
                        .build()
        );

        // 토큰 발급
        return tokenService.createTokenResponseFrom(savedUser);
    }
}