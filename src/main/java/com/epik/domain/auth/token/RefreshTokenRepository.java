package com.epik.domain.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long id);

    void deleteByUserIdAndToken(Long userId, String token);
}
