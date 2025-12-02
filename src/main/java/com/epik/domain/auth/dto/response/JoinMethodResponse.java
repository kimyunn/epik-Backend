package com.epik.domain.auth.dto.response;

import com.epik.domain.auth.entity.enums.UserJoinType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JoinMethodResponse {
    private String email;
    private boolean registered;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String joinMethod;  // "email", "kakao", "naver", "google", null

    public static JoinMethodResponse notRegistered(String email) {
        return JoinMethodResponse.builder()
                .email(email)
                .registered(false)
                .joinMethod(null)
                .build();
    }

    public static JoinMethodResponse registered(String email, UserJoinType type) {
        return JoinMethodResponse.builder()
                .email(email)
                .registered(true)
                .joinMethod(type.name().toLowerCase())
                .build();
    }
}
