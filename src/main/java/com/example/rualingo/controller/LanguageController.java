package com.example.rualingo.controller;

import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LanguageDTO;
import com.example.rualingo.DTO.VocabularyDTO;
import com.example.rualingo.service.LanguageService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/languages")
@PreAuthorize("hasRole('ADMIN')")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @PostMapping
    public ResponseEntity<LanguageDTO> createLanguage(@RequestBody LanguageDTO languageDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(languageDTO));
    }

    @GetMapping
    public List<LanguageDTO> getAllLanguages() {
        return languageService.getAllLanguages();
    }

    @GetMapping("/{languageId}")
    public LanguageDTO getLanguageById(@PathVariable Long languageId) {
        return languageService.getLanguageById(languageId);
    }

    @PutMapping("/{languageId}")
    public LanguageDTO updateLanguage(@PathVariable Long languageId, @RequestBody LanguageDTO languageDTO) {
        return languageService.updateLanguage(languageId, languageDTO);
    }

    @DeleteMapping("/{languageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLanguage(@PathVariable Long languageId) {
        languageService.deleteLanguage(languageId);
    }

    @GetMapping("/{languageId}/courses")
    public List<CourseDTO> getCoursesForLanguage(@PathVariable Long languageId) {
        return languageService.getCoursesForLanguage(languageId);
    }

    @GetMapping("/{languageId}/vocabulary")
    public List<VocabularyDTO> getVocabularyForLanguage(@PathVariable Long languageId) {
        return languageService.getVocabularyForLanguage(languageId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
