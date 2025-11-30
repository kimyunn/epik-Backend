package com.epik.global.common.dto;

import com.epik.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ExceptionDto {
    private String code;             // 커스텀 에러 -> 클라이언트가 로직 분기시 사용
    private String message;          // 에러 메시지

    // of() 정적메서드
    public static ExceptionDto of(ErrorCode errorCode) {
        return ExceptionDto.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}
