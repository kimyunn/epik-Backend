package com.epik.domain.auth.controller;

import com.epik.domain.auth.dto.request.LoginRequest;
import com.epik.domain.auth.dto.request.SignupRequest;
import com.epik.domain.auth.dto.response.EmailAvailabilityResponse;
import com.epik.domain.auth.dto.response.JoinMethodResponse;
import com.epik.domain.auth.dto.response.NicknameAvailabilityResponse;
import com.epik.domain.auth.dto.response.TokenResponse;
import com.epik.domain.auth.service.AuthService;
import com.epik.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated // 클래스 레벨, @RequestParam, @PathVariable에 필요
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/email/available")
    public ResponseEntity<ApiResponse<EmailAvailabilityResponse>> checkEmailAvailability(
            @RequestParam
            @Email(message = "이메일을 @까지 정확하게 입력해 주세요")
            String email) {
        EmailAvailabilityResponse response = authService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/nickname/available")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
            @RequestParam
            @NotBlank(message = "닉네임을 입력해주세요")
            @Size(min = 2, max = 16, message = "닉네임은 2-16자 사이여야 합니다")
            String nickname) {
        NicknameAvailabilityResponse response = authService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success());
    }

    @GetMapping("/check-join")
    public ResponseEntity<ApiResponse<JoinMethodResponse>> checkJoinMethod(
            @RequestParam
            @Email(message = "이메일을 @까지 정확하게 입력해 주세요")
            String email) {
        JoinMethodResponse response = authService.checkJoinMethod(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
