package com.epik.domain.auth.controller;

import com.epik.domain.auth.dto.request.SignupRequest;
import com.epik.domain.auth.dto.response.EmailAvailabilityResponse;
import com.epik.domain.auth.dto.response.JoinMethodResponse;
import com.epik.domain.auth.dto.response.NicknameAvailabilityResponse;
import com.epik.domain.auth.service.AuthService;
import com.epik.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/email/available")
    public ResponseEntity<ApiResponse<EmailAvailabilityResponse>> checkEmailAvailability(@RequestParam String email) {
        EmailAvailabilityResponse response = authService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/nickname/available")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(@RequestParam String nickname) {
        NicknameAvailabilityResponse response = authService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success());
    }

    @GetMapping("/check-join")
    public ResponseEntity<ApiResponse<JoinMethodResponse>> checkJoinMethod(@RequestParam
            String email) {
        JoinMethodResponse response = authService.checkJoinMethod(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
