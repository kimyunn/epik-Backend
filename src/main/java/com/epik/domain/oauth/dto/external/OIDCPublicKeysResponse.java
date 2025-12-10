package com.epik.domain.oauth.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class OIDCPublicKeysResponse {
    List<OIDCPublicKey> keys;
}
