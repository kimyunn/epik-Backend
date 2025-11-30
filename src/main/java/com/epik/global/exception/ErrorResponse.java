package com.epik.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 기본 에러 응답 형식
 * {
 *   "status": 409,
 *   "code": "A-001",
 *   "message": "이미 존재하는 이메일입니다.",
 *   "path": "/api/signup",
 *   "timestamp": "2025-01-15T12:34:56.789"
 * }
 */
@Getter
@Builder
public class ErrorResponse {

    private int status;              // 상태 코드
    private String code;             // 에러 코드 -> 클라이언트가 로직 분기시 사용
    private String message;          // 에러 메시지

    private String path;             // 요청 URI
    private LocalDateTime timestamp; // 에러 발생 시간

    // of() 정적메서드
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
