package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetEmailRequest {
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일을 @까지 정확하게 입력해 주세요")
    private String email;
}
