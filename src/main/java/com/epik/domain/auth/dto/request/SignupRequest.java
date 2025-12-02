package com.epik.domain.auth.dto.request;

import lombok.Getter;

@Getter
public class SignupRequest {

    String email;
    String password;
    String nickname;

    /**
     * 필수 이용약관
     */
    private boolean termsOfServiceAgreed;    // 서비스 이용 약관
    private boolean privacyPolicyAgreed;    // 개인정보 수집 및 이용 동의
    private boolean locationServiceAgreed;     // 위치기반서비스 이용약관

    /**
     * 선택 이용 약관
     */
    private Boolean marketingConsent;    // 마케팅 수신 동의 여부 -> 사용자가 체크하지 않으면 null 가능
}
