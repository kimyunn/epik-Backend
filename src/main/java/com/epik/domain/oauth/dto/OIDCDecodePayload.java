package com.epik.domain.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OIDCDecodePayload {
    private String iss; // id 토큰을 발급한 인증 기관 정보
    private String aud; // id 토큰이 발급된 앱의 앱키
    private String sub; // id 토큰에 해당하는 사용자의 회원 번호
    //    private Long iat; // id 토큰 발급 또는 갱신 시각
//    private Long exp; // id 토큰 만료 시간
//    private String nonce; // 인가 코드 요청 시 전달한 nonce 갑과 동일한 값
//    private Long auth_time; // 사용자가 카카오 로그인으로 인증을 완료한 시각
    private String email;
    private String name; // 닉네임
//    private String picture; // 프로필 미리보기 이미지 url
}
