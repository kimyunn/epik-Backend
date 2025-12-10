package com.epik.domain.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON에서 제외
public class SocialCheckResponse {
    private String status;

    // 기존 회원용
    private String accessToken;
    private String refreshToken;

    // 신규 회원용
    private String registerToken;

    // 정적 팩토리 메서드
    public static SocialCheckResponse loginSuccess(String accessToken, String refreshToken) {
        return SocialCheckResponse.builder()
                .status("LOGIN_SUCCESS")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static SocialCheckResponse needSignup(String registerToken) {
        return SocialCheckResponse.builder()
                .status("NEED_SIGNUP")
                .registerToken(registerToken)
                .build();
    }
}
