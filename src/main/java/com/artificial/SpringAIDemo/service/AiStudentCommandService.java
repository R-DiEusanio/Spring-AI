package com.artificial.SpringAIDemo.service;

import com.artificial.SpringAIDemo.client.DiscentiClient;
import com.artificial.SpringAIDemo.data.DiscenteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiStudentCommandService {

    private static final Logger log = LoggerFactory.getLogger(AiStudentCommandService.class);

    private final ChatClient chatClient;
    private final DiscentiClient discentiClient;
    private final VectorStore vectorStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiStudentCommandService(ChatClient chatClient,
                                   DiscentiClient discentiClient,
                                   VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.discentiClient = discentiClient;
        this.vectorStore = vectorStore;
    }

    public String handleStudentCreation(String userInput) {
        String extractionPrompt = """
                Extract the following data from the input text:
                - first name
                - last name
                - age
                - city of residence
                - matricola

                Format the response as JSON with keys:
                {"nome": "...", "cognome": "...", "eta": ..., "cittaResidenza": "...", "matricola": "..."}

                Input: %s
                """.formatted(userInput);

        String rawOutput = chatClient.prompt()
                .user(extractionPrompt)
                .call()
                .content();

        log.info("LLM raw output: {}", rawOutput);

        String jsonOutput = rawOutput
                .replaceAll("(?i)```json", "")
                .replaceAll("```", "")
                .trim();

        try {
            Map<String, Object> values = objectMapper.readValue(jsonOutput, Map.class);

            DiscenteDTO nuovo = new DiscenteDTO();
            nuovo.setNome((String) values.get("nome"));
            nuovo.setCognome((String) values.get("cognome"));
            nuovo.setEta((Integer) values.get("eta"));
            nuovo.setCittaResidenza((String) values.get("cittaResidenza"));
            nuovo.setMatricola((String) values.get("matricola"));

            DiscenteDTO saved = discentiClient.createDiscente(nuovo);
            log.info("Studente creato nel microservizio: {}", saved.getMatricola());

            String testo = "%s %s is a %d-year-old student from %s, with matricola %s."
                    .formatted(saved.getNome(), saved.getCognome(), saved.getEta(),
                            saved.getCittaResidenza(), saved.getMatricola());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("nome", saved.getNome());
            metadata.put("cognome", saved.getCognome());
            metadata.put("eta", saved.getEta());
            metadata.put("città", saved.getCittaResidenza());
            metadata.put("matricola", saved.getMatricola());
            metadata.put("tipo", "studente_singolo");

            vectorStore.add(List.of(new Document(testo, metadata)));

            return "Student " + saved.getNome() + " " + saved.getCognome() + " created successfully.";

        } catch (Exception e) {
            log.error("Errore nella creazione del nuovo studente", e);
            return "Errore nella creazione dello studente: " + e.getMessage();
        }
    }

    public String handleStudentUpdate(String userInput) {
        String updatePrompt = """
Extract structured data from the following sentence about a student.
You must return a JSON object with these fields:
- nome
- cognome
- eta
- cittaResidenza
- matricola

Format:
{"nome": "...", "cognome": "...", "eta": ..., "cittaResidenza": "...", "matricola": "..."}

Text: %s
""".formatted(userInput);


        try {
            String rawOutput = chatClient.prompt()
                    .user(updatePrompt)
                    .call()
                    .content();

            log.info("LLM raw output (update): {}", rawOutput);

            String jsonOutput = rawOutput
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();

            Map<String, Object> values = objectMapper.readValue(jsonOutput, Map.class);

            DiscenteDTO dto = new DiscenteDTO();
            dto.setNome((String) values.get("nome"));
            dto.setCognome((String) values.get("cognome"));
            dto.setEta((Integer) values.get("eta"));
            dto.setCittaResidenza((String) values.get("cittaResidenza"));
            dto.setMatricola((String) values.get("matricola"));

            DiscenteDTO updated = discentiClient.updateDiscente(dto.getMatricola(), dto);
            log.info("Studente aggiornato nel microservizio: {}", updated.getMatricola());

            return "Student " + updated.getNome() + " " + updated.getCognome() + " updated successfully.";

        } catch (Exception e) {
            log.error("Errore nell'aggiornamento dello studente", e);
            return "Errore nell'aggiornamento dello studente: " + e.getMessage();
        }
    }

    public String handleStudentDeletion(String userInput) {
        String deletePrompt = """
Extract the student’s *matricola* (ID code) from the following sentence.
Return a JSON object like this: {"matricola": "MR123"}

Text: %s
""".formatted(userInput);


        try {
            String rawOutput = chatClient.prompt()
                    .user(deletePrompt)
                    .call()
                    .content();

            log.info("LLM raw output (delete): {}", rawOutput);

            String jsonOutput = rawOutput
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();

            Map<String, Object> values = objectMapper.readValue(jsonOutput, Map.class);
            String matricola = (String) values.get("matricola");

            discentiClient.deleteDiscente(matricola);
            log.info("Studente eliminato nel microservizio: {}", matricola);

            return "Student with matricola " + matricola + " deleted successfully.";

        } catch (Exception e) {
            log.error("Errore nella cancellazione dello studente", e);
            return "Errore nella cancellazione dello studente: " + e.getMessage();
        }
    }
}
