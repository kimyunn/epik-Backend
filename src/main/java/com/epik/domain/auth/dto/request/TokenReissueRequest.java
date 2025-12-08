package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenReissueRequest {
    @NotBlank(message = "refreshToken은 필수 값입니다.")
    private String refreshToken;
}
