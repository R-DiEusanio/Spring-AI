package com.artificial.SpringAIDemo.service;

import com.artificial.SpringAIDemo.data.DiscenteDTO;
import com.artificial.SpringAIDemo.client.DiscentiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final DiscentiClient discenteClient;

    @Autowired
    public AiService(ChatClient chatClient, DiscentiClient discenteClient) {
        this.chatClient = chatClient;
        this.discenteClient = discenteClient;
    }

    public String answerWithContext(String domanda, List<DiscenteDTO> discenti) {
        String context = discenti.stream()
                .map(d -> d.getNome() + " " + d.getCognome() + " (" + d.getCittaResidenza() + ")")
                .collect(Collectors.joining("\n"));

        String prompt = "Ecco una lista di studenti:\n" + context + "\n\nDomanda: " + domanda;

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public String ask(String domanda) {
        return chatClient.prompt()
                .user(domanda)
                .call()
                .content();
    }

    public String answerForCity(String domanda, String citta) {
        List<DiscenteDTO> discenti = discenteClient.getDiscentiByCitta(citta);
        return answerWithContext(domanda, discenti);
    }
}



