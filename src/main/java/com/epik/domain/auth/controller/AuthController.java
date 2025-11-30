package com.epik.domain.auth.controller;

import com.epik.domain.auth.dto.response.AvailabilityCheckResponse;
import com.epik.domain.auth.service.AuthService;
import com.epik.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/email/available")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponse>> checkEmailAvailability(@RequestParam String email) {
        AvailabilityCheckResponse available = authService.isEmailAvailable(email);

        return ResponseEntity.ok(ApiResponse.success(available));
    }

}
