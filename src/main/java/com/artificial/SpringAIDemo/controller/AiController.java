package com.artificial.SpringAIDemo.controller;

import com.artificial.SpringAIDemo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askAI(@RequestBody String question) {
        String rispostaConContext = aiService.answerUsingVectorStore(question);

        if (rispostaConContext.trim().equalsIgnoreCase("No data available.")) {
            String rispostaGenerale = aiService.ask(question);
            return ResponseEntity.ok(rispostaGenerale);
        }

        return ResponseEntity.ok(rispostaConContext);
    }
}
