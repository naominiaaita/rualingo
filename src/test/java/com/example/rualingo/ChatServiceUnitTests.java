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

class ChatServiceUnitTests {

    @Test
    void processInput_stripsPunctuationForVocabLookup() {
        VocabularyRepository vocabularyRepository = mock(VocabularyRepository.class);
        LanguageService languageService = mock(LanguageService.class);
        CourseService courseService = mock(CourseService.class);
        LessonService lessonService = mock(LessonService.class);
        ActivityLogService activityLogService = mock(ActivityLogService.class);
        com.example.rualingo.repository.LanguageRepository languageRepository = mock(com.example.rualingo.repository.LanguageRepository.class);
        com.example.rualingo.repository.UserRepository userRepository = mock(com.example.rualingo.repository.UserRepository.class);
        com.example.rualingo.repository.ChatLogRepository chatLogRepository = mock(com.example.rualingo.repository.ChatLogRepository.class);
        com.example.rualingo.repository.LessonRepository lessonRepository = mock(com.example.rualingo.repository.LessonRepository.class);
        com.example.rualingo.ai.AiTutorService aiTutorService = mock(com.example.rualingo.ai.AiTutorService.class);

        Vocabulary vocab = new Vocabulary();
        vocab.setTranslation("hello");

        when(vocabularyRepository.findByWord("gude")).thenReturn(Optional.of(vocab));

        ChatService chatService = new ChatService(vocabularyRepository, languageService, courseService, lessonService, activityLogService, languageRepository, userRepository, chatLogRepository, lessonRepository, aiTutorService);

        String reply = chatService.processInput("Gude!!!", 1L);
        assertThat(reply).contains("I recognize 'gude'");
    }
}
