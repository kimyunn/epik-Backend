package com.epik.global.common.dto;

import com.epik.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.Set;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Boolean success;
    private T data;
    private ErrorResponse error;

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
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorResponse.of(errorCode))
                .build();

    }

    /**
     * @Valid 유효성 검증 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, BindingResult bindingResult) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorResponse.of(errorCode, bindingResult))
                .build();
    }

    /**
     * @RequestParma, @PathVariable 유효성 검증 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, Set<ConstraintViolation<?>> violationIterator) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorResponse.of(errorCode, violationIterator))
                .build();
    }

}
