package com.epik.domain.oauth.dto.request;

import lombok.Getter;

@Getter
public class SocialSignupRequest {
    private String registerToken; // 임시 회원 식별용 토큰
    private String nickname;
}
