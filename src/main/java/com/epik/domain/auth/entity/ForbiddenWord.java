package com.epik.domain.auth.entity;

import com.epik.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "forbidden_words")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForbiddenWord extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String word;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean isActive = true;
}
