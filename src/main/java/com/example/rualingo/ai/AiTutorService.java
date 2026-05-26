package com.example.rualingo.ai;

import com.example.rualingo.model.ChatLog;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * AI Tutor Service - Provides conversational language learning support.
 * Uses a local, custom-built chatbot engine for:
 * - Conversational language practice
 * - Question and response interaction
 * - Pronunciation and sentence assistance
 * - Reinforcement of lesson content
 */
@Service
public class AiTutorService {

    private final LocalChatbotEngine chatbotEngine;

    public AiTutorService(LocalChatbotEngine chatbotEngine) {
        this.chatbotEngine = chatbotEngine;
    }

    /**
     * Check if AI tutor is enabled and configured.
     */
    public boolean isEnabled() {
        return chatbotEngine.isConfigured();
    }

    /**
     * Generate a reply to the user's message based on the lesson context and language.
     * Supports conversational learning with pronunciation, sentence correction, and vocabulary assistance.
     */
    public String reply(
            String userMessage,
            Language targetLanguageOrNull,
            Lesson lessonOrNull,
            List<ChatLog> recentChatHistoryNewestFirst,
            String username) {
        
        if (!isEnabled()) {
            return null;
        }

        return chatbotEngine.generateResponse(
                userMessage,
                targetLanguageOrNull,
                lessonOrNull,
                recentChatHistoryNewestFirst != null ? recentChatHistoryNewestFirst : List.of(),
                username);
    }
}
