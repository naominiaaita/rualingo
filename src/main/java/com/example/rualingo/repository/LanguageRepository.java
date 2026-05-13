package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.Language;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {

}
