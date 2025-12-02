package com.epik.domain.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일을 @까지 정확하게 입력해 주세요")
    String email;

    @Size(min = 8, max = 15, message = "비밀번호는 8-15자 사이여야 합니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*?_]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다"
    )
    String password;

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
//    @NotBlank
    @NotNull(message = "서비스 이용 약관은 필수입니다.")
    private boolean termsOfServiceAgreed;    // 서비스 이용 약관
    @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다.")
    private boolean privacyPolicyAgreed;    // 개인정보 수집 및 이용 동의
    @NotNull(message = "위치 기반 서비스 약관 동의는 필수입니다.")
    private boolean locationServiceAgreed;     // 위치기반서비스 이용약관

    /**
     * 선택 이용 약관
     */
    private Boolean marketingConsent;    // 마케팅 수신 동의 여부 -> 사용자가 체크하지 않으면 null 가능
}