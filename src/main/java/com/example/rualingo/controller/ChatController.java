package com.example.rualingo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.rualingo.model.ChatMessage;
import com.example.rualingo.service.ChatService;
import com.example.rualingo.repository.ChatLogRepository;
import com.example.rualingo.model.ChatLog;




    @RestController
@RequestMapping("/api/chat")


public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @PostMapping("/ask")
    public ChatMessage askRua(@RequestBody ChatMessage userMessage) {
        // 1. Get the "intelligent" response from Service
        String ruaAnswer = chatService.processInput(userMessage.getUserQuery());
        
        // 2. Set the answer in the object to send back to Android
        userMessage.setResponse(ruaAnswer);

        // 3. LOGGING (Crucial for Marks): Save to your MySQL chat_logs table
        ChatLog log = new ChatLog(userMessage.getUserQuery(), ruaAnswer);
        chatLogRepository.save(log);

        return userMessage;
    }
}



