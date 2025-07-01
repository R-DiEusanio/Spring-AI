package com.artificial.SpringAIDemo.service;

import com.artificial.SpringAIDemo.client.DiscentiClient;
import com.artificial.SpringAIDemo.data.DiscenteDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final DiscentiClient discentiClient;
    private final VectorStore vectorStore;

    public AiService(ChatClient chatClient,
                     DiscentiClient discentiClient,
                     VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.discentiClient = discentiClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public String answerWithDiscentiContext(String question, List<DiscenteDTO> discenti) {
        String context = discenti.stream()
                .map(d -> d.getNome() + " " + d.getCognome() + " (City: " + d.getCittaResidenza() + ")")
                .collect(Collectors.joining("\n"));

        String prompt = """
                Answer the question based *only* on the following list of students.
                If the answer is not in the list, reply: "No data available."

                Students list:
                %s

                Question: %s
                """.formatted(context, question);

        String fullResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return extractAnswer(fullResponse);
    }

    public String answerForCity(String question, String city) {
        List<DiscenteDTO> filtered = discentiClient.getDiscentiByCitta(city);
        return answerWithDiscentiContext(question, filtered);
    }

    public String answerUsingVectorStore(String question) {
        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7d)
                .topK(5)
                .build();

        List<Document> documents = retriever.retrieve(new org.springframework.ai.rag.Query(question));

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a knowledge assistant.
                List all students from the context provided.
                Only use the context to answer.
                If there are no students listed, reply: "No data available."
                Return the names in a comma-separated list.
                Do not explain or add commentary

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        String fullResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return extractAnswer(fullResponse);
    }

    private String extractAnswer(String response) {
        String lower = response.toLowerCase();
        int idx = lower.indexOf("answer:");
        if (idx != -1) {
            return response.substring(idx + 7).trim().replaceAll("(?s)(?i)^.*?Answer:\\s*", "");
        }
        return response.trim();
    }
}
