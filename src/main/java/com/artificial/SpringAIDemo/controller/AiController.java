package com.artificial.SpringAIDemo.controller;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller REST semplice che mantiene la cronologia della chat in memoria
 * e genera risposte AI con tutto lo storico inviato al modello.
 * Adatto a demo e prototipi single-user.
 */


@RestController
public class AiController {
    private final ChatModel chatModel;
    private final List<UserMessage> messageHistory;

    public AiController(@Qualifier("ollamaChatModel") ChatModel chatModel) {
        this.chatModel = chatModel;
        this.messageHistory = new ArrayList<>();
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        UserMessage userMsg = new UserMessage(userMessage);
        messageHistory.add(userMsg);
        Prompt prompt = new Prompt(new ArrayList<>(messageHistory));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}


