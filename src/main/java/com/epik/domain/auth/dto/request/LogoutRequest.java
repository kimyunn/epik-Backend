package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogoutRequest {

    @NotBlank(message = "RefreshToken은 필수입니다.")
    private String refreshToken;
}
