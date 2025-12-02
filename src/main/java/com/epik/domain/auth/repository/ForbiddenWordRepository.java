package com.epik.domain.auth.repository;

import com.epik.domain.auth.entity.ForbiddenWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Long> {
    @Query("SELECT f.word FROM ForbiddenWord f")
    List<String> findAllWords();
}
