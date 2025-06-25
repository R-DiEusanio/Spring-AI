package com.artificial.SpringAIDemo.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class AiController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final List<UserMessage> history = new ArrayList<>();

    public AiController(
            @Qualifier("ollamaChatModel") ChatModel chatModel,
            VectorStore vectorStore
    ) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public String chat(@RequestBody String userQuestion) {
        // Eseguo retrieval dal vector store
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userQuestion)
                        .topK(4)
                        .build()
        );

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        String enriched = """
            Usa il seguente contesto per rispondere all'utente:
            %s

            Domanda: %s
            """
                .formatted(context, userQuestion);

        history.add(new UserMessage(enriched));

        Prompt prompt = new Prompt(new ArrayList<>(history));
        return chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();
    }
}
