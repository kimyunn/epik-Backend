package com.epik.domain.oauth.repository;

import com.epik.domain.oauth.entity.SocialLoginProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginProviderRepository extends JpaRepository<SocialLoginProvider, Long> {

    /**
     * 프로바이더 이름으로 조회
     */
    Optional<SocialLoginProvider> findByProviderName(String providerName);
}
