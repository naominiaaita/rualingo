package com.example.rualingo.service;

import com.example.rualingo.DTO.VocabularyDTO;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.Vocabulary;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.VocabularyRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final LanguageRepository languageRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public VocabularyService(VocabularyRepository vocabularyRepository, LanguageRepository languageRepository, CourseRepository courseRepository, LessonRepository lessonRepository) {
        this.vocabularyRepository = vocabularyRepository;
        this.languageRepository = languageRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    public VocabularyDTO createVocabulary(VocabularyDTO dto) {
        Vocabulary savedVocabulary =
                Objects.requireNonNull(vocabularyRepository.save(toEntity(dto)), "Saved vocabulary must not be null");
        return toDTO(savedVocabulary);
    }

    @Transactional(readOnly = true)
    public VocabularyDTO getVocabularyById(Long vocabularyId) {
        return toDTO(requireVocabulary(vocabularyId));
    }

    @Transactional(readOnly = true)
    public List<VocabularyDTO> getAllVocabulary() {
        return vocabularyRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyDTO> getVocabularyByLanguageAndTopic(Long languageId, String topic) {
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        String requiredTopic = Objects.requireNonNull(topic, "topic must not be null").trim().toLowerCase();
        return languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId))
                .getVocabularies().stream()
                .filter(v -> v.getTopic() != null && requiredTopic.equalsIgnoreCase(v.getTopic().trim()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VocabularyDTO updateVocabulary(Long vocabularyId, VocabularyDTO dto) {
        Vocabulary vocabulary = requireVocabulary(vocabularyId);
        if (dto.getWordTarget() != null) {
            vocabulary.setWordTarget(dto.getWordTarget());
        }
        if (dto.getWord() != null) {
            vocabulary.setWord(dto.getWord());
        }
        if (dto.getPhonetic() != null) {
            vocabulary.setPhonetic(dto.getPhonetic());
        }
        if (dto.getExampleSentence() != null) {
            vocabulary.setExampleSentence(dto.getExampleSentence());
        }
        if (dto.getTranslation() != null) {
            vocabulary.setTranslation(dto.getTranslation());
        }
        if (dto.getLanguageId() != null) {
            Language language = languageRepository.findById(dto.getLanguageId())
                    .orElseThrow(() -> new NoSuchElementException("Language not found: " + dto.getLanguageId()));
            vocabulary.setLanguage(language);
        }
        if (dto.getCourseId() != null) {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new NoSuchElementException("Course not found: " + dto.getCourseId()));
            vocabulary.setCourse(course);
        }
        if (dto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + dto.getLessonId()));
            vocabulary.setLesson(lesson);
            if (lesson.getCourse() != null) {
                vocabulary.setCourse(lesson.getCourse());
            }
        }
        Vocabulary savedVocabulary =
                Objects.requireNonNull(vocabularyRepository.save(vocabulary), "Saved vocabulary must not be null");
        return toDTO(savedVocabulary);
    }

    public void deleteVocabulary(Long vocabularyId) {
        vocabularyRepository.delete(requireVocabulary(vocabularyId));
    }

    public VocabularyDTO assignLanguage(Long vocabularyId, Long languageId) {
        Vocabulary vocabulary = requireVocabulary(vocabularyId);
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        Language language = languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
        vocabulary.setLanguage(language);
        Vocabulary savedVocabulary =
                Objects.requireNonNull(vocabularyRepository.save(vocabulary), "Saved vocabulary must not be null");
        return toDTO(savedVocabulary);
    }

    @Transactional(readOnly = true)
    public List<VocabularyDTO> getVocabularyByLanguage(Long languageId) {
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        return languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId))
                .getVocabularies().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Vocabulary> findEntityById(Long vocabularyId) {
        Long requiredVocabularyId = Objects.requireNonNull(vocabularyId, "vocabularyId must not be null");
        return vocabularyRepository.findById(requiredVocabularyId);
    }

    public VocabularyDTO toDTO(Vocabulary vocabulary) {
        return new VocabularyDTO(
                vocabulary.getId(),
                vocabulary.getWordTarget(),
                vocabulary.getWord(),
                vocabulary.getPhonetic(),
                vocabulary.getExampleSentence(),
                vocabulary.getTranslation(),
                vocabulary.getLanguage() != null ? vocabulary.getLanguage().getId() : null,
                vocabulary.getCourse() != null ? vocabulary.getCourse().getId() : null,
                vocabulary.getLesson() != null ? vocabulary.getLesson().getId() : null,
                vocabulary.getTopic());
    }

    public Vocabulary toEntity(VocabularyDTO dto) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWordTarget(dto.getWordTarget());
        vocabulary.setWord(dto.getWord());
        vocabulary.setPhonetic(dto.getPhonetic());
        vocabulary.setExampleSentence(dto.getExampleSentence());
        vocabulary.setTranslation(dto.getTranslation());
        vocabulary.setTopic(dto.getTopic());

        if (dto.getLanguageId() != null) {
            Language language = languageRepository.findById(dto.getLanguageId())
                    .orElseThrow(() -> new NoSuchElementException("Language not found: " + dto.getLanguageId()));
            vocabulary.setLanguage(language);
        }
        if (dto.getCourseId() != null) {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new NoSuchElementException("Course not found: " + dto.getCourseId()));
            vocabulary.setCourse(course);
        }
        if (dto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + dto.getLessonId()));
            vocabulary.setLesson(lesson);
            if (lesson.getCourse() != null) {
                vocabulary.setCourse(lesson.getCourse());
            }
        }

        return vocabulary;
    }

    private Vocabulary requireVocabulary(Long vocabularyId) {
        Long requiredVocabularyId = Objects.requireNonNull(vocabularyId, "vocabularyId must not be null");
        return vocabularyRepository.findById(requiredVocabularyId)
                .orElseThrow(() -> new NoSuchElementException("Vocabulary not found: " + vocabularyId));
    }
}
