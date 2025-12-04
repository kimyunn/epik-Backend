package com.epik.domain.auth.entity;

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
     * 토큰 사용 처리
     */
    public void use() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

}
