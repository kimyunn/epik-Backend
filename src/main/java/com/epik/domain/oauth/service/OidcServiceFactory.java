package com.epik.domain.oauth.service;

import com.epik.domain.oauth.dto.SocialProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OidcServiceFactory {

    // 각 Provider(KAKAO, GOOGLE)에 해당하는 OIDC 서비스 구현체를 보관
    // key : provider 이름, value : AbstractOidcService 구현체
    private final Map<SocialProvider, SocialAuthProvider> serviceMap;

    /**
     * 스프링 컨테이너는 AbstractOidcService 타입을 구현한 모든 빈을 자동으로 수집하여
     * services 리스트로 주입한다.
     *
     * 예) KakaoOidcService, GoogleOidcService 가 @Service로 등록되어 있다면
     *     두 구현체 모두 자동으로 리스트에 포함됨.
     */
    public OidcServiceFactory(List<SocialAuthProvider> services) {
        // 빈 Map 생성
        this.serviceMap = new HashMap<>();

        // 각 서비스가 반환하는 providerName(KAKAO, GOOGLE)을 key로 사용하여 매핑 구성
        for (SocialAuthProvider provider : services) {
            SocialProvider providerName = provider.getProviderName();
            serviceMap.put(providerName, provider);
        }
    }

    /**
     * 전달받은 provider(KAKAO, GOOGLE)에 해당하는 OIDC 서비스 구현체 반환
     * → 컨트롤러/서비스에서 특정 Provider의 OIDC 로직을 호출할 때 사용됨
     */
    public SocialAuthProvider getOidcService(SocialProvider provider) {
        return serviceMap.get(provider);
    }
}
