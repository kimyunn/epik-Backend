package com.epik.domain.oauth.service;

import com.epik.domain.oauth.client.NaverOauthClient;
import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.dto.SocialUserInfo;
import com.epik.domain.oauth.dto.response.NaverUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NaverOAuth2Service implements SocialAuthProvider{

    private final NaverOauthClient naverOauthClient;

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        NaverUserInfoResponse response = naverOauthClient.getUserInfo("Bearer " + accessToken);
        NaverUserInfoResponse.Response userInfo = response.getResponse();
        return new SocialUserInfo(
                "https://nid.naver.com",
                null,
                userInfo.getId(),
                userInfo.getEmail(),
                userInfo.getName()
        );
    }

    @Override
    public SocialProvider getProviderName() {
        return SocialProvider.NAVER;
    }
}
