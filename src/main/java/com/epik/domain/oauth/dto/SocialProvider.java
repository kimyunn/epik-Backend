package com.epik.domain.oauth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SocialProvider {
    KAKAO, GOOGLE;

    @JsonCreator
    public static SocialProvider from(String value) {
        return SocialProvider.valueOf(value.toUpperCase());
    }
}
