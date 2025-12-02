package com.epik.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // (접근제한자 protected로 된) 기본 생성자를 자동으로 생성
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder 전용
@Builder
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean isAgreed;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    /*
     * 외래키 관계 설정 (ManyToOne)
     * User 테이블과의 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * 외래키 관계 설정 (ManyToOne)
     * ConsentItem 테이블과의 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_item_id", nullable = false)
    private ConsentItem consentItem;

    public static UserConsent create(User user, ConsentItem consentItem, Boolean isAgreed) {
        return UserConsent.builder()
                .user(user)
                .consentItem(consentItem)
                .isAgreed(isAgreed)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
