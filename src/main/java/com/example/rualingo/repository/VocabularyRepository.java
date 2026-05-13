package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.Vocabulary;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long>{

    Optional<Vocabulary> findByWord(String word);

}
