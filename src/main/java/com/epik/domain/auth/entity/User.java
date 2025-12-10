package com.epik.domain.auth.entity;

import com.epik.domain.auth.entity.enums.UserJoinType;
import com.epik.domain.auth.entity.enums.UserRole;
import com.epik.domain.auth.entity.enums.UserStatus;
import com.epik.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // (접근제한자 protected로 된) 기본 생성자를 자동으로 생성
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder 전용
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    // join_type
    @Enumerated(EnumType.STRING)
    @Column(name = "join_type", nullable = false)
    private UserJoinType joinType;

    @Column(length = 500)
    private String introduction;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    //role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "notice_last_checked_at")
    private LocalDateTime noticeLastCheckedAt;

    @Column(name = "notification_last_checked_at")
    private LocalDateTime notificationLastCheckedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static User createEmailUser(String email, String password, String nickname) {
        return User.builder()
                .joinType(UserJoinType.EMAIL)
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
    }


    public static User createSocialUser(String email, String nickname) {
        return User.builder()
                .joinType(UserJoinType.SOCIAL)
                .email(email)
                .nickname(nickname)
                .build();
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}
