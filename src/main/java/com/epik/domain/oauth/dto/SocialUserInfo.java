package com.epik.domain.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserInfo {
    private String sub;
    private String email;
}
