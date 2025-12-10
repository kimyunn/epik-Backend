package com.epik.domain.oauth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialRegisterPayload {
    private String provider;
    private String socialId;
    private String email;
    private String nickname;
}
