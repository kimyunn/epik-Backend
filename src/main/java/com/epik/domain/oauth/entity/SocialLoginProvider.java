package com.epik.domain.oauth.entity;

import com.epik.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "social_login_providers")
@NoArgsConstructor
public class SocialLoginProvider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false, unique = true, length = 50)
    private String providerName;

    // 활성화 관리
    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1") // DB에 DEFAULT 1 설정
    private boolean active = true;
}

