package com.epik.domain.oauth.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class JwksResponse {
    private List<Jwk> keys;
}
