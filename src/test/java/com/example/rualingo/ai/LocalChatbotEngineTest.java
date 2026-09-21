package com.example.rualingo.ai;

import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalChatbotEngineTest {

    private LocalChatbotEngine chatbotEngine;

    @BeforeEach
    void setUp() {
        chatbotEngine = new LocalChatbotEngine();
    }

    // ============ FEATURE 1: Conversational Language Practice ============

    @Test
    void testConversationalPractice_Greeting() {
        Language language = new Language("Tok Pisin");
        String response = chatbotEngine.generateResponse("Hello", language, null, List.of(), "TestUser");
        assertNotNull(response);
        assertTrue(response.contains("Rua"));
    }

    // ============ FEATURE 2: Question and Response Interaction ============

    @Test
    void testQuestionResponse_Vocabulary() {
        Language language = new Language("French");
        String response = chatbotEngine.generateResponse("What does bonjour mean?", language, null, List.of(), "TestUser");
        assertNotNull(response);
        assertTrue(response.length() > 10);
    }

    // ============ FEATURE 3: Pronunciation Assistance ============

    @Test
    void testPronunciation_Request() {
        Language language = new Language("Spanish");
        String response = chatbotEngine.generateResponse("How do I pronounce hola?", language, null, List.of(), "TestUser");
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("pronounc") || response.toLowerCase().contains("phonetic"));
    }

    // ============ FEATURE 4: Lesson Reinforcement ============

    @Test
    void testLessonReinforcement_Content() {
        Language language = new Language("German");
        Lesson lesson = new Lesson();
        lesson.setTitle("Greetings");
        lesson.setContent("Guten Tag, Hallo");
        lesson.setTopic("Basic Greetings");
        
        String response = chatbotEngine.generateResponse("Tell me about this lesson", language, lesson, List.of(), "TestUser");
        assertNotNull(response);
        assertTrue(response.length() > 20);
    }

    @Test
    void testAiTutorServiceIntegration() {
        AiTutorService aiTutorService = new AiTutorService(chatbotEngine);
        assertTrue(aiTutorService.isEnabled());
        
        Language language = new Language("Italian");
        String response = aiTutorService.reply("Ciao", language, null, List.of(), "TestUser");
        assertNotNull(response);
    }

    @Test
    void testEdgeCases() {
        String response1 = chatbotEngine.generateResponse("", null, null, List.of(), null);
        assertNotNull(response1);
        
        String response2 = chatbotEngine.generateResponse(null, null, null, List.of(), null);
        assertNotNull(response2);
    }

    // Regression: "how do I say hello" must trigger pronunciation help, not the generic greeting menu.
    @Test
    void testHowDoISayHello_ReturnsPronunciationHelp_NotGenericGreeting() {
        Language language = new Language("Tok Pisin");
        String response = chatbotEngine.generateResponse("how do I say hello", language, null, List.of(), "TestUser");
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("phonetic") || response.toLowerCase().contains("syllable"));
        assertFalse(response.contains("What can I help you with today?"));
    }
}
