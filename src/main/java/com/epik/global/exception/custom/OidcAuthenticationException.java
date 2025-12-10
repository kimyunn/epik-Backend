package com.epik.global.exception.custom;

import com.epik.global.exception.ErrorCode;

public class OidcAuthenticationException extends BusinessException{

    public OidcAuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
