package com.epik.global.exception.custom;

import com.epik.global.exception.ErrorCode;

public class OAuth2AuthenticationException extends BusinessException{

    public OAuth2AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
