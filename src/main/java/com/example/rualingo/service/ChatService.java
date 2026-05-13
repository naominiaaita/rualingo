package com.example.rualingo.service;

import org.springframework.stereotype.Service; // This fixes Line 4
import org.springframework.beans.factory.annotation.Autowired; // This fixes Line 6
import java.util.Optional; // This fixes Line 15
import com.example.rualingo.repository.VocabularyRepository; // This fixes Line 7 & 15
import com.example.rualingo.model.Vocabulary;

@Service
public class ChatService {
    @Autowired
    private VocabularyRepository vocabularyRepository;

    public String processInput(String input) {
        // Simple NLP: split sentence into words
        String[] words = input.toLowerCase().split("\\s+");
        
        for (String word : words) {
            // Check your MySQL table for this specific word
            Optional<Vocabulary> match = vocabularyRepository.findByWord(word);
            
            if (match.isPresent()) {
                return "Rua says: I recognize '" + word + "'! In English, that's '" 
                       + match.get().getTranslation() + "'. Keep practicing! 💎";
            }
        }
        return "Rua says: I'm not sure about that word yet. Want to try a greeting?";
    }
}
