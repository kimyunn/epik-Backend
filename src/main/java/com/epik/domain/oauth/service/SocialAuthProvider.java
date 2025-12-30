package com.epik.domain.oauth.service;

import com.epik.domain.oauth.dto.enums.SocialProvider;
import com.epik.domain.oauth.dto.SocialUserInfo;

public interface SocialAuthProvider {

    SocialUserInfo getUserInfo(String token);
    SocialProvider getProviderName();
}
