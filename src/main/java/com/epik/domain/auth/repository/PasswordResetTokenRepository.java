package com.epik.domain.auth.repository;

import com.epik.domain.auth.entity.PasswordResetToken;
import com.epik.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUserAndUsedFalse(User user);
}
