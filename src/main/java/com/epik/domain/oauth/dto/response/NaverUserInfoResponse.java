package com.epik.domain.oauth.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponse {
    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String id;     // 유니크한 일련번호
        private String email;
        private String name;
    }
}
