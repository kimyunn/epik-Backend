package com.epik.domain.oauth.dto;

import com.epik.domain.oauth.dto.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialRegisterPayload {
    private SocialProvider provider;
    private String providerUserId;
    private String email;
    private String nickname;
}
