package com.epik.domain.oauth.service;

import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.entity.ConsentItem;
import com.epik.domain.auth.entity.User;
import com.epik.domain.auth.entity.UserConsent;
import com.epik.domain.auth.entity.enums.ConsentItemCode;
import com.epik.domain.auth.repository.ConsentItemRepository;
import com.epik.domain.auth.repository.UserConsentRepository;
import com.epik.domain.auth.repository.UserRepository;
import com.epik.domain.auth.service.TokenService;
import com.epik.domain.oauth.dto.SocialRegisterPayload;
import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.enums.SocialProvider;
import com.epik.domain.oauth.dto.request.SocialSignupRequest;
import com.epik.domain.oauth.dto.response.SocialCheckResponse;
import com.epik.domain.oauth.entity.SocialLogin;
import com.epik.domain.oauth.repository.SocialLoginRepository;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialAuthProviderFactory providerFactory;
    private final SocialLoginRepository socialLoginRepository;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ConsentItemRepository consentItemRepository;
    private final UserConsentRepository userConsentRepository;

    @Transactional(readOnly = true)
    public SocialCheckResponse handleSocialLogin(String providerName, String token) {

        SocialProvider provider = SocialProvider.from(providerName);

        SocialAuthProvider socialAuthProvider = providerFactory.getProvider(provider);
        SocialUserInfo userInfo = socialAuthProvider.getUserInfo(token);
        // 여기서 해야할 일은
        // 1. 기존 회원인지 체크하기
        // 2. 기존 회원이면 엑세스/리프레시 토큰 발급
        // 3. 신규 회원이면 레지스터 토큰 발급

        // 1. 기존 회원인지 체크
        // social_logins table에 provider + socialId(provider_user_id)로 동일한 데이터가 존재하는 지 확인
        Optional<User> userOpt = socialLoginRepository.findUserBySocialIdAndProvider(userInfo.getSub(), provider);

        // 2. 기존 회원이라면
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 토큰 생성해서
            TokenResponse tokenResponseFrom = tokenService.createTokenResponseFrom(user);
            // 응답
            return SocialCheckResponse.loginSuccess(
                    tokenResponseFrom.getAccessToken(),
                    tokenResponseFrom.getRefreshToken()
            );
        } else {
            // 신규 회원 처리
            // 레지스터 토큰 발급
            return SocialCheckResponse.needSignup(
                    tokenService.createRegisterToken(provider, userInfo.getSub(), userInfo.getEmail()),
                    userInfo.getEmail()
            );
        }
    }

    @Transactional
    public TokenResponse completeSocialSignup(SocialSignupRequest request) {
        SocialRegisterPayload payload = tokenService.decodeRegisterToken(request.getRegisterToken());
        SocialProvider provider = payload.getProvider();
        String providerUserId = payload.getProviderUserId(); // sub
        String tokenEmail = payload.getEmail();

        // 이미 가입된 소셜인지 재확인 (중복 가입 방지)
        Optional<User> existing = socialLoginRepository.findUserBySocialIdAndProvider(providerUserId, provider);

        if (existing.isPresent()) {
            // 이미 가입되어 있으면: 가입 대신 로그인 토크 발급으로 처리
            return tokenService.createTokenResponseFrom(existing.get());
        }

        // 저장할 email 결정 (토큰 email이 우선)
        String emailToSave = resolveEmailToSave(tokenEmail, request.getEmail());


        // 3) 추출한 정보로 DB에 저장
        // 일단 User table 저장
        User socialUser = User.createSocialUser(emailToSave, request.getNickname());
        User savedUser = userRepository.save(socialUser);

        // UserConsents table 저장
        saveAllUserConsent(savedUser, request);

        // SocialLogin table 저장
        socialLoginRepository.save(
                SocialLogin.builder()
                        .user(savedUser)
                        .provider(provider)
                        .socialId(payload.getProviderUserId())
                        .build()
        );

        // 토큰 발급
        return tokenService.createTokenResponseFrom(savedUser);
    }

    private String resolveEmailToSave(String tokenEmail, String requestEmail) {

        // 토큰에 email이 있으면 무조건 그걸 신뢰 (프론트가 보낸 email 무시)
        if (tokenEmail != null && !tokenEmail.isBlank()) {
            return tokenEmail;
        }

        if (requestEmail == null || requestEmail.isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED);
        }

        return requestEmail;
    }

    private void saveAllUserConsent(User user, SocialSignupRequest request) {
        // 필수 약관 3개 → 동의 이력 3개
        saveUserConsent(user, ConsentItemCode.TERMS, request.getTermsOfServiceAgreed());
        saveUserConsent(user, ConsentItemCode.PRIVACY, request.getPrivacyPolicyAgreed());
        saveUserConsent(user, ConsentItemCode.LOCATION, request.getLocationServiceAgreed());

        // 선택 약관 (동의 시)
        if (Boolean.TRUE.equals(request.getMarketingConsent())) {
            saveUserConsent(user, ConsentItemCode.MARKETING, true);
        }
    }

    private void saveUserConsent(User user, ConsentItemCode code, Boolean isAgreed) {
        ConsentItem consentItem = consentItemRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSENT_ITEM_NOT_FOUND));

        UserConsent userConsent = UserConsent.create(user, consentItem, isAgreed);

        userConsentRepository.save(userConsent);
    }
}