package com.epik.global.exception;

import com.epik.global.common.dto.ApiResponse;
import com.epik.global.common.dto.ErrorResponse;
import com.epik.global.exception.custom.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {

        // 로깅 추가!
        log.warn("Validation failed for request: {} {}, errors: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", ")));

        ApiResponse<ErrorResponse> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // @RequestParam, @PathVariable 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {

        // 로깅 추가!
        log.warn("Constraint violation for request: {} {}, violations: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", ")));

        ApiResponse<ErrorResponse> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, e.getConstraintViolations());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // 서비스단 비즈니스 로직 예외 처리 4xx 코드
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("Business Exception: code={}, message={}, path={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        ApiResponse<ErrorResponse> response = ApiResponse.error(errorCode);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception: path={}", request.getRequestURI(), e);

        ApiResponse<ErrorResponse> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
