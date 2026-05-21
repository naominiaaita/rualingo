package com.example.rualingo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.rualingo.model.Vocabulary;
import com.example.rualingo.repository.VocabularyRepository;
import com.example.rualingo.service.ActivityLogService;
import com.example.rualingo.service.ChatService;
import com.example.rualingo.service.CourseService;
import com.example.rualingo.service.LanguageService;
import com.example.rualingo.service.LessonService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ChatServiceUnitTests {

    @Test
    void processInput_stripsPunctuationForVocabLookup() {
        VocabularyRepository vocabularyRepository = mock(VocabularyRepository.class);
        LanguageService languageService = mock(LanguageService.class);
        CourseService courseService = mock(CourseService.class);
        LessonService lessonService = mock(LessonService.class);
        ActivityLogService activityLogService = mock(ActivityLogService.class);

        Vocabulary vocab = new Vocabulary();
        vocab.setTranslation("hello");

        when(vocabularyRepository.findByWord("gude")).thenReturn(Optional.of(vocab));

        ChatService chatService = new ChatService();
        ReflectionTestUtils.setField(chatService, "vocabularyRepository", vocabularyRepository);
        ReflectionTestUtils.setField(chatService, "languageService", languageService);
        ReflectionTestUtils.setField(chatService, "courseService", courseService);
        ReflectionTestUtils.setField(chatService, "lessonService", lessonService);
        ReflectionTestUtils.setField(chatService, "activityLogService", activityLogService);

        String reply = chatService.processInput("Gude!!!", 1L);
        assertThat(reply).contains("I recognize 'gude'");
    }
}
