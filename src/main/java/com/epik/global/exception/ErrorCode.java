package com.epik.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common (C-XXX)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C-001", "입력값이 올바르지 않습니다."),
    PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "C-002", "존재하지 않는 소셜 로그인 제공자입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-500", "서버 내부 오류가 발생했습니다."),

    // Auth - Login & Register (A-XXX)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "A-001", "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "A-002", "이미 존재하는 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A-003", "회원을 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A-004", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // OIDC - ID Token (O-XXX)
    MALFORMED_ID_TOKEN(HttpStatus.BAD_REQUEST, "O-001", "ID 토큰 형식이 올바르지 않습니다."),
    EXPIRED_ID_TOKEN(HttpStatus.UNAUTHORIZED, "O-002", "ID 토큰이 만료되었습니다."),
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "O-003", "ID 토큰이 유효하지 않습니다."),
    OIDC_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "O-500", "ID 토큰 처리 중 오류가 발생했습니다."),

    // Token - Access/Refresh (T-XXX)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "T-001", "유효하지 않은 인증 정보입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T-002", "인증이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "T-003", "인증 정보를 찾을 수 없습니다. 다시 로그인해 주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "T-004", "인증이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_REGISTER_TOKEN(HttpStatus.UNAUTHORIZED, "T-005", "회원가입 정보가 유효하지 않습니다. 처음부터 다시 진행해 주세요."),

    // Password (P-XXX)
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "P-001", "비밀번호 재설정 링크가 만료되었습니다."),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "P-002", "유효하지 않은 비밀번호 재설정 요청입니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "P-003", "현재 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
