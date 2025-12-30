package com.epik.domain.oauth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class SocialSignupRequest {
    @NotBlank(message = "Register 토큰은 필수입니다.")
    private String registerToken;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    String email;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 16, message = "닉네임은 2-16자 사이여야 합니다")
    @Pattern(
            regexp = "^[가-힣a-zA-Z.,_@]+$",
            message = "한글, 영문(대소문자), 특수문자(.,_,@)만 사용 가능합니다."
    )
    String nickname;

    /**
     * 필수 이용약관
     */
    @NotNull(message = "서비스 이용 약관은 필수입니다.")
    private Boolean termsOfServiceAgreed;
    @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다.")
    private Boolean privacyPolicyAgreed;
    @NotNull(message = "위치 기반 서비스 약관 동의는 필수입니다.")
    private Boolean locationServiceAgreed;

    /**
     * 선택 이용 약관
     */
    private Boolean marketingConsent;
}
