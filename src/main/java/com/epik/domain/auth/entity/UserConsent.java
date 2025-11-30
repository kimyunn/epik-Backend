package com.epik.domain.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
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
}
