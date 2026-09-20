package com.example.rualingo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Language;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByNameContainingIgnoreCase(String name);
    Optional<Language> findByName(String name);
}
