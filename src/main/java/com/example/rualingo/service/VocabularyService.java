package com.example.rualingo.service;

import com.example.rualingo.DTO.TranslationResultDTO;
import com.example.rualingo.DTO.VocabularyDTO;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.Vocabulary;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.VocabularyRepository;
import java.util.ArrayList;
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
    public List<VocabularyDTO> getVocabularyByFilter(Long courseId, String topic, Long lessonId) {
        if (lessonId != null) {
            return vocabularyRepository.findByLessonId(lessonId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        // Fallback or more specific filters could go here
        return getAllVocabulary();
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
        if (dto.getTopic() != null) {
            vocabulary.setTopic(dto.getTopic());
        }
        if (dto.getAudioPath() != null) {
            vocabulary.setAudioPath(dto.getAudioPath());
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

    @Transactional(readOnly = true)
    public List<TranslationResultDTO> translateEnglish(String english, Long languageId) {
        String requiredEnglish = Objects.requireNonNull(english, "english must not be null").trim();
        if (requiredEnglish.isBlank()) {
            throw new IllegalArgumentException("english must not be blank");
        }

        List<Language> targetLanguages;
        if (languageId != null) {
            Language language = languageRepository.findById(languageId)
                    .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
            targetLanguages = List.of(language);
        } else {
            targetLanguages = languageRepository.findAll();
        }

        List<TranslationResultDTO> results = new ArrayList<>();
        for (Language language : targetLanguages) {
            Long id = language.getId();
            if (id == null) {
                continue;
            }
            List<Vocabulary> hits = vocabularyRepository.findByWordIgnoreCaseAndLanguageId(requiredEnglish, id);
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByTranslationIgnoreCaseAndLanguageId(requiredEnglish, id);
            }
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByWordContainingIgnoreCaseAndLanguageId(requiredEnglish, id);
            }
            if (hits.isEmpty()) {
                hits = vocabularyRepository.findByTranslationContainingIgnoreCaseAndLanguageId(requiredEnglish, id);
            }
            if (hits.isEmpty()) {
                continue;
            }
            Vocabulary vocab = hits.get(0);
            String translated = vocab.getWordTarget() != null && !vocab.getWordTarget().isBlank()
                    ? vocab.getWordTarget()
                    : (vocab.getWord() != null ? vocab.getWord() : vocab.getTranslation());
            results.add(new TranslationResultDTO(id, language.getName(), requiredEnglish, translated));
        }
        return results;
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
                vocabulary.getTopic(),
                vocabulary.getAudioPath());
    }

    public Vocabulary toEntity(VocabularyDTO dto) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWordTarget(dto.getWordTarget());
        vocabulary.setWord(dto.getWord());
        vocabulary.setPhonetic(dto.getPhonetic());
        vocabulary.setExampleSentence(dto.getExampleSentence());
        vocabulary.setTranslation(dto.getTranslation());
        vocabulary.setTopic(dto.getTopic());
        vocabulary.setAudioPath(dto.getAudioPath());

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
