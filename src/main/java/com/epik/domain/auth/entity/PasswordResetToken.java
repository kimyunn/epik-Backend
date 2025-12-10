package com.epik.domain.auth.entity;

import com.epik.global.exception.custom.BusinessException;
import com.epik.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PasswordResetToken {

    private static final int EXPIRE_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /*
     * 외래키 관계 설정 (ManyToOne)
     * User 테이블과의 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 토큰 생성
     */
    public static PasswordResetToken create(User user) {
        LocalDateTime now = LocalDateTime.now();

        return PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .createdAt(now)
                .expiresAt(now.plusMinutes(EXPIRE_MINUTES))
                .build();

    }

    /**
     * 토큰 유효성 검증 (사용 여부, 만료 여부) 및 사용 처리
     */
    public void validate() {
        validateNotUsed();
        validateNotExpired();
    }

    private void validateNotUsed() {
        if (this.used) {
            throw new BusinessException(ErrorCode.TOKEN_ALREADY_USED);
        }
    }

    private void validateNotExpired() {
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    /**
     * 토큰 사용 처리
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
