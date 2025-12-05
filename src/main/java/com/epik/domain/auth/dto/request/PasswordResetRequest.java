package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordResetRequest {

    @NotBlank(message = "비밀번호 재설정 토큰이 필요합니다")
    private String token;

    @Size(min = 8, max = 15, message = "비밀번호는 8-15자 사이여야 합니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*?_]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다"
    )
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String newPasswordConfirm;

    @AssertTrue(message = "비밀번호가 일치하지 않습니다")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

}
