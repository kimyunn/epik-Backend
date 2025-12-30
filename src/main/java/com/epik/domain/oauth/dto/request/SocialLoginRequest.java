package com.epik.domain.oauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialLoginRequest {
    @NotBlank(message = "소셜 로그인 토큰은 필수입니다.")
    private String token;
}
