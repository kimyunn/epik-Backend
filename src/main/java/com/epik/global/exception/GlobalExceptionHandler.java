package com.epik.global.exception;

import com.epik.global.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 서비스단 비즈니스 로직 예외 처리 4xx 코드
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("Business Exception: code={}, message={}, path={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        ApiResponse<Void> response = ApiResponse.error(errorCode);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception: path={}", request.getRequestURI(), e);

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
