package com.example.rualingo.service;

import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.VocabularyDTO;
import com.example.rualingo.model.Language;
import com.example.rualingo.repository.LanguageRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    public LanguageDTO createLanguage(LanguageDTO dto) {
        Language language = toEntity(dto);
        Language savedLanguage = languageRepository.save(language);
        return toDTO(savedLanguage);
    }

    @Transactional(readOnly = true)
    public LanguageDTO getLanguageById(Long languageId) {
        Language language = Objects.requireNonNull(requireLanguage(languageId), "Language must not be null");
        return toDTO(language);
    }

    @Transactional(readOnly = true)
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LanguageDTO updateLanguage(Long languageId, LanguageDTO dto) {
        Language language = Objects.requireNonNull(requireLanguage(languageId), "Language must not be null");
        if (dto.getName() != null && !dto.getName().isBlank()) {
            language.setName(dto.getName());
        }
        if (dto.getProvince() != null) {
            language.setProvince(dto.getProvince());
        }
        if (dto.getDistrict() != null) {
            language.setDistrict(dto.getDistrict());
        }
        if (dto.getClan() != null) {
            language.setClan(dto.getClan());
        }
        if (dto.getFlag() != null) {
            language.setFlag(dto.getFlag());
        }
        Language savedLanguage = Objects.requireNonNull(languageRepository.save(language), "Saved language must not be null");
        return toDTO(savedLanguage);
    }

    public void deleteLanguage(Long languageId) {
        Language language = Objects.requireNonNull(requireLanguage(languageId), "Language must not be null");
        languageRepository.delete(language);
    }

    @Transactional(readOnly = true)
    public Optional<Language> findEntityById(Long languageId) {
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        return languageRepository.findById(requiredLanguageId);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesForLanguage(Long languageId) {
        Language language = Objects.requireNonNull(requireLanguage(languageId), "Language must not be null");
        return language.getCourses().stream()
                .map(course -> new CourseDTO(
                        course.getId(),
                        course.getTitle(),
                        course.getName(),
                        course.getDescription(),
                        course.getLanguage() != null ? course.getLanguage().getId() : null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyDTO> getVocabularyForLanguage(Long languageId) {
        Language language = Objects.requireNonNull(requireLanguage(languageId), "Language must not be null");
        return language.getVocabularies().stream()
                .map(vocabulary -> new VocabularyDTO(
                        vocabulary.getId(),
                        vocabulary.getWordTarget(),
                        vocabulary.getWord(),
                        vocabulary.getPhonetic(),
                        vocabulary.getExampleSentence(),
                        vocabulary.getTranslation(),
                        vocabulary.getLanguage() != null ? vocabulary.getLanguage().getId() : null,
                        vocabulary.getCourse() != null ? vocabulary.getCourse().getId() : null,
                        vocabulary.getLesson() != null ? vocabulary.getLesson().getId() : null,
                        vocabulary.getTopic()))
                .collect(Collectors.toList());
    }

    public LanguageDTO toDTO(Language language) {
        return new LanguageDTO(
                language.getId(),
                language.getName(),
                language.getProvince(),
                language.getDistrict(),
                language.getClan(),
                language.getFlag(),
                language.getUser() != null ? language.getUser().getId() : null);
    }

    public Language toEntity(LanguageDTO dto) {
        Language language = new Language();
        language.setName(dto.getName());
        language.setProvince(dto.getProvince());
        language.setDistrict(dto.getDistrict());
        language.setClan(dto.getClan());
        language.setFlag(dto.getFlag());
        return language;
    }

    private Language requireLanguage(Long languageId) {
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        return languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
    }
}
