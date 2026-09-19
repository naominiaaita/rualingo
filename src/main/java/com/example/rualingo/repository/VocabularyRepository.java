package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Vocabulary;
import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long>{

    Optional<Vocabulary> findByWord(String word);

    List<Vocabulary> findByWordIgnoreCase(String word);

    List<Vocabulary> findByTranslationIgnoreCase(String translation);

    List<Vocabulary> findByWordIgnoreCaseAndLanguageId(String word, Long languageId);

    List<Vocabulary> findByTranslationIgnoreCaseAndLanguageId(String translation, Long languageId);

    List<Vocabulary> findByWordContainingIgnoreCaseAndLanguageId(String word, Long languageId);

    List<Vocabulary> findByTranslationContainingIgnoreCaseAndLanguageId(String translation, Long languageId);

    List<Vocabulary> findByLessonId(Long lessonId);

    List<Vocabulary> findByCourseIdAndTopicAndLessonId(Long courseId, String topic, Long lessonId);
}
