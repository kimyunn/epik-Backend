package com.epik.domain.oauth.service;

import com.epik.domain.oauth.client.NaverOauthClient;
import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.enums.SocialProvider;
import com.epik.domain.oauth.dto.external.NaverUserInfoResponse;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.OAuth2AuthenticationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuth2Provider implements SocialAuthProvider{

    private final NaverOauthClient naverOauthClient;

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {

        if (accessToken == null || accessToken.isBlank()) {
            throw new OAuth2AuthenticationException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            NaverUserInfoResponse response = naverOauthClient.getUserInfo("Bearer " + accessToken);

            if (!response.isSuccess()) {
                log.error("네이버 사용자 정보 조회 실패 - code: {}, message: {}",
                        response.getResultcode(), response.getMessage());
                throw new OAuth2AuthenticationException(ErrorCode.OAUTH2_USER_INFO_FAILED);
            }

            NaverUserInfoResponse.Response userInfo = response.getResponse();

            if (userInfo == null || userInfo.getId() == null) {
                throw new OAuth2AuthenticationException(ErrorCode.OAUTH2_USER_INFO_FAILED);
            }

            return new SocialUserInfo(userInfo.getId(), userInfo.getEmail());
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (FeignException.Unauthorized e) {
            log.error("네이버 Access Token 인증 실패", e);
            throw new OAuth2AuthenticationException(ErrorCode.SOCIAL_TOKEN_INVALID);
        } catch (FeignException e) {
            log.error("네이버 API 호출 실패 - status: {}", e.status(), e);
            throw new OAuth2AuthenticationException(ErrorCode.PROVIDER_API_ERROR);
        }

    }

    @Override
    public SocialProvider getProviderName() {
        return SocialProvider.NAVER;
    }
}
