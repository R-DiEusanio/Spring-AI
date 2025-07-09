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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final ChatClient chatClient;
    private final DiscentiClient discentiClient;
    private final VectorStore vectorStore;
    private final AiStudentCommandService aiStudentCommandService;

    public AiService(ChatClient chatClient,
                     DiscentiClient discentiClient,
                     VectorStore vectorStore,
                     AiStudentCommandService aiStudentCommandService) {
        this.chatClient = chatClient;
        this.discentiClient = discentiClient;
        this.vectorStore = vectorStore;
        this.aiStudentCommandService = aiStudentCommandService;
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

        return extractAnswer(
                chatClient.prompt().user(prompt).call().content()
        );
    }

    public String answerForCity(String question, String city) {
        List<DiscenteDTO> filtered = discentiClient.getDiscentiByCitta(city);
        return answerWithDiscentiContext(question, filtered);
    }

    public String answerUsingVectorStore(String question) {
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7d)
                .topK(5)
                .build();

        List<Document> documents = retriever.retrieve(new org.springframework.ai.rag.Query(question));

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a helpful assistant that can act in two modes:
                1. **General chat** — be friendly, helpful, and informal.
                2. **Student assistant** — when given context about students, use that data to answer questions exactly.

                Use *general chat mode* if the question is unrelated to student records.
                Use *student assistant mode* if the question refers to students, names, ages, cities, or matrix.

                When in student assistant mode:
                - Use *only* the context below.
                - If the answer is in the context, provide full details.
                - If not, reply: "No data available."
                - If multiple students appear multiple times, list each only once. Do not repeat.
                - Always preserve real data from context (name, age, city, matrix).
                - Example: "Who is Marco Rossi?" → "Marco Rossi is a 22‑year‑old student from Rome, matrix AB123."

                Context:
                %s

                User question:
                %s
                """.formatted(context, question);

        return chatClient.prompt().user(prompt).call().content().trim();
    }

    private String extractAnswer(String response) {
        int idx = response.toLowerCase().indexOf("answer:");
        if (idx != -1) {
            return response.substring(idx + 7).trim().replaceAll("(?s)(?i)^.*?Answer:\\s*", "");
        }
        return response.trim();
    }

    public String processUserInput(String userInput) {
        String classificationPrompt = """
        You are a classifier. Classify the user input into one of these categories:
        - CREATE → for commands to create/add a student
        - UPDATE → for commands to modify a student's data
        - DELETE → for commands to delete a student
        - CHAT → for general questions or conversation

        Examples:
        "Add student Marco Rossi, 22, from Rome, matricola MR123" → CREATE  
        "Update student with matricola MR123 to age 25" → UPDATE  
        "Delete student with matricola MR123" → DELETE  
        "Who are the students from Rome?" → CHAT

        Input: %s

        Respond ONLY with one of: CREATE, UPDATE, DELETE, CHAT.
        """.formatted(userInput);

        String intent = chatClient.prompt()
                .user(classificationPrompt)
                .call()
                .content()
                .trim()
                .toUpperCase();

        log.info("INPUT UTENTE: {}", userInput);
        log.info("INTENT IDENTIFICATO: {}", intent);

        if (intent.contains("DELETE")) {
            return aiStudentCommandService.handleStudentDeletion(userInput);
        } else if (intent.contains("CREATE")) {
            return aiStudentCommandService.handleStudentCreation(userInput);
        } else if (intent.contains("UPDATE")) {
            return aiStudentCommandService.handleStudentUpdate(userInput);
        } else {
            return ask(userInput);
        }
    }



}
