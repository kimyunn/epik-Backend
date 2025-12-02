package com.epik.domain.auth.repository;

import com.epik.domain.auth.entity.ConsentItem;
import com.epik.domain.auth.entity.enums.ConsentItemCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentItemRepository extends JpaRepository <ConsentItem, Long> {
    Optional<ConsentItem> findByCodeAndIsActiveTrue(ConsentItemCode code);
}
