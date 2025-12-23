package com.epik.domain.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserInfo {
    private String iss;
    private String aud;
    private String sub;
    private String email;
    private String name; // 닉네임
//    private String picture; // 프로필
}
