package com.epik.domain.oauth.service;

import com.epik.domain.oauth.dto.enums.SocialProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SocialAuthProviderFactory {

    private final Map<SocialProvider, SocialAuthProvider> serviceMap;

    public SocialAuthProviderFactory(List<SocialAuthProvider> services) {
        this.serviceMap = new HashMap<>();

        for (SocialAuthProvider provider : services) {
            SocialProvider providerName = provider.getProviderName();
            serviceMap.put(providerName, provider);
        }
    }

    public SocialAuthProvider getProvider(SocialProvider provider) {
        return serviceMap.get(provider);
    }
}
