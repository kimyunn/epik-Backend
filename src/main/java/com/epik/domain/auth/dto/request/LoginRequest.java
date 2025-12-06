package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일을 @까지 정확하게 입력해 주세요")
    String email;

    @Size(min = 8, max = 15, message = "비밀번호는 8-15자 사이여야 합니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*?_]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다"
    )
    String password;
}
