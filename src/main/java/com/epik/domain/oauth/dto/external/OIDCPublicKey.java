package com.epik.domain.oauth.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class OIDCPublicKey {

    private String kid;
    private String alg;
    private String use;
    private String n;
    private String e;
}

