package com.epik.domain.oauth.dto.enums;

import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.BusinessException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum SocialProvider {
    KAKAO, GOOGLE, NAVER;

    @JsonCreator
    public static SocialProvider from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PROVIDER_REQUIRED);
        }

        try {
            return SocialProvider.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PROVIDER);
        }
    }
}