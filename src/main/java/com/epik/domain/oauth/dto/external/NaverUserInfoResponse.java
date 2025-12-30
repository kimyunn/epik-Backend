package com.epik.domain.oauth.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponse {
    private String resultcode;
    private String message;
    private Response response;

    public boolean isSuccess() {
        return "00".equals(resultcode);
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String id;
        private String email;
    }
}
