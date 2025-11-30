package com.epik.domain.auth.repository;

import com.epik.domain.auth.entity.ConsentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentItemRepository extends JpaRepository <ConsentItem, Long> {
}
