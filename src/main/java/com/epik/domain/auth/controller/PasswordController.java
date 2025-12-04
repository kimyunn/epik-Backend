package com.epik.domain.auth.controller;

import com.epik.domain.auth.dto.request.PasswordResetEmailRequest;
import com.epik.domain.auth.service.PasswordService;
import com.epik.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    /**
     * 비밀번호 재설정 이메일 발송
     */
    @PostMapping("/reset/email")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetEmail(@RequestBody PasswordResetEmailRequest request) {
        passwordService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success());
    }


}
