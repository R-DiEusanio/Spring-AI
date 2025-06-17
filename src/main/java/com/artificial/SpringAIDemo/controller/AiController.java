package com.artificial.SpringAIDemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/chat")
public class AiController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;


    public AiController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<String> chat(@PathVariable String sessionId, @RequestBody String userMessage) {

        List<Message> history = chatMemory.get(sessionId);

        UserMessage newMessage = new UserMessage(userMessage);

        List<Message> messages = new ArrayList<>(history);
        messages.add(newMessage);

        var response = chatClient.prompt()
                .messages(messages)
                .call();

        chatMemory.add(sessionId,List.of(newMessage,new AssistantMessage(response.content())));
        return ResponseEntity.ok(response.content());

    }
}


