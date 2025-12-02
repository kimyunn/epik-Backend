package com.epik.domain.auth.entity;

import com.epik.domain.auth.entity.enums.ConsentItemCode;
import com.epik.global.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "consent_items")
public class ConsentItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ConsentItemCode code;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isRequired;

    @Column(length = 20, nullable = false)
    private String version;

    @Column(nullable = false)
    private Boolean isActive;
}
