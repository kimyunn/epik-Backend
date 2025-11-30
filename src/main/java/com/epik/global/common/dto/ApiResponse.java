package com.epik.global.common.dto;

import com.epik.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Boolean success;
    private T data;
    private ExceptionDto error;

    /**
     * 성공 응답 생성 (데이터만)
     */
    public static <T> ApiResponse<T> success(final T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .build();
    }

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ExceptionDto.of(errorCode))
                .build();

    }

}
