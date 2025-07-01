package com.artificial.SpringAIDemo.controller;

import com.artificial.SpringAIDemo.client.DiscentiClient;
import com.artificial.SpringAIDemo.data.DiscenteDTO;
import com.artificial.SpringAIDemo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class AiController {

    @Autowired
    private DiscentiClient discentiClient;

    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public ResponseEntity<String> askAI(@RequestBody String domanda) {
        if (domanda.toLowerCase().contains("students who come from teramo")) {
            List<DiscenteDTO> teramani = discentiClient.getDiscentiByCitta("Teramo");
            String risposta = aiService.answerWithContext(domanda, teramani);
            return ResponseEntity.ok(risposta);
        }

        return ResponseEntity.ok(aiService.ask(domanda));
    }
}
