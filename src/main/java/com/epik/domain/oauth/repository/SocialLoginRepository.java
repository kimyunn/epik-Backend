package com.epik.domain.oauth.repository;

import com.epik.domain.auth.entity.User;
import com.epik.domain.oauth.dto.enums.SocialProvider;
import com.epik.domain.oauth.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

    @Query("""
        SELECT sl.user
        FROM SocialLogin sl
        WHERE sl.socialId = :socialId
          AND sl.provider = :provider
    """)
    Optional<User> findUserBySocialIdAndProvider(
            @Param("socialId") String socialId,
            @Param("provider") SocialProvider provider
    );
}
