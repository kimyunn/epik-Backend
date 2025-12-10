package com.epik.domain.oauth.repository;

import com.epik.domain.oauth.dto.SocialProvider;
import com.epik.domain.oauth.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

    /**
     * 소셜 ID와 프로바이더 ID로 소셜 로그인 정보 조회
     */
//    Optional<SocialLogin> findBySocialIdAndProviderId(String socialId, Long providerId);

    /**
     * 회원 ID로 소셜 로그인 정보 조회
     */
//    Optional<SocialLogin> findByUserId(Long userId);

    /**
     * 소셜 ID와 프로바이더 이름으로 소셜 로그인 정보 조회
     */
    @Query("SELECT sl FROM SocialLogin sl " +
            "JOIN FETCH sl.provider p " +  // N+1 방지를 위한 FETCH JOIN
            "WHERE sl.socialId = :socialId AND p.providerName = :providerName")
    Optional<SocialLogin> findBySocialIdAndProviderName(
            @Param("socialId") String socialId,
            @Param("providerName") SocialProvider providerName
    );
}
