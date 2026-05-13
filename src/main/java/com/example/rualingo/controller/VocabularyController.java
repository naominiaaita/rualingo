package com.example.rualingo.controller;

import com.example.rualingo.DTO.VocabularyDTO;
import com.example.rualingo.service.VocabularyService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    // Students can see all vocabulary or filter by language
    @GetMapping
    public List<VocabularyDTO> getAllVocabulary(
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) String topic) {
        if (languageId != null && topic != null && !topic.isBlank()) {
            return vocabularyService.getVocabularyByLanguageAndTopic(languageId, topic);
        }
        if (languageId != null) {
            return vocabularyService.getVocabularyByLanguage(languageId);
        }
        return vocabularyService.getAllVocabulary();
    }

    // Students can see a specific word
    @GetMapping("/{vocabularyId}")
    public VocabularyDTO getVocabularyById(@PathVariable Long vocabularyId) {
        return vocabularyService.getVocabularyById(vocabularyId);
    }

    // ADMIN ONLY: Create a new word
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabularyDTO> createVocabulary(@RequestBody VocabularyDTO vocabularyDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vocabularyService.createVocabulary(vocabularyDTO));
    }

    // ADMIN ONLY: Update a word
    @PutMapping("/{vocabularyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public VocabularyDTO updateVocabulary(@PathVariable Long vocabularyId, @RequestBody VocabularyDTO vocabularyDTO) {
        return vocabularyService.updateVocabulary(vocabularyId, vocabularyDTO);
    }

    // ADMIN ONLY: Delete a word
    @DeleteMapping("/{vocabularyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVocabulary(@PathVariable Long vocabularyId) {
        vocabularyService.deleteVocabulary(vocabularyId);
    }

    // ADMIN ONLY: Move a word to a different language
    @PatchMapping("/{vocabularyId}/language/{languageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public VocabularyDTO assignLanguage(@PathVariable Long vocabularyId, @PathVariable Long languageId) {
        return vocabularyService.assignLanguage(vocabularyId, languageId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
