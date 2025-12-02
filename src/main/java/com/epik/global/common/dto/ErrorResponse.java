package com.epik.global.common.dto;

import com.epik.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Builder
public class ErrorResponse {
    private String code;             // 커스텀 에러 -> 클라이언트가 로직 분기시 사용
    private String message;          // 에러 메시지

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldError> fieldErrors;  // 유효성 검증용

    /**
     * 일반 에러
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * @Valid 유효성 검증 실패
     */
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        // 1. 원본 에러 목록 가져오기
        List<org.springframework.validation.FieldError> originalErrors =
                bindingResult.getFieldErrors();

        // 2. 결과를 담을 빈 리스트 생성
        List<FieldError> fieldErrors = new ArrayList<>();

        // 3. 하나씩 반복하면서 처리
        for (org.springframework.validation.FieldError error : originalErrors) {

            // 4. value 값 처리
            String value = "";
            if (error.getRejectedValue() != null) {
                value = error.getRejectedValue().toString();
            }

            // 5. FieldError 객체 생성
            FieldError fieldError = FieldError.builder()
                    .field(error.getField())
                    .value(value)
                    .reason(error.getDefaultMessage())
                    .build();

            // 6. 리스트에 추가
            fieldErrors.add(fieldError);
        }

        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .fieldErrors(fieldErrors)  // 필드 에러 포함
                .build();
    }

    /**
     * @RequestParam, @PathVariable 검증 실패
     */
    public static ErrorResponse of(ErrorCode errorCode, Set<ConstraintViolation<?>> violationIterator) {
        List<FieldError> fieldErrors = new ArrayList<>();

        for (ConstraintViolation<?> violation : violationIterator) {

            // value 처리 (null이면 빈 문자열)
            String value = violation.getInvalidValue() != null ?
                    violation.getInvalidValue().toString() : "";

            // FieldError 생성하고 추가
            fieldErrors.add(
                    FieldError.builder()
                            .field(extractFieldName(violation))
                            .value(value)
                            .reason(violation.getMessage())
                            .build()
            );
        }

        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .fieldErrors(fieldErrors)
                .build();
    }

    private static String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex > 0 ? path.substring(lastDotIndex + 1) : path;
    }

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
    }
}
