package com.artificial.SpringAIDemo.loader;

import com.artificial.SpringAIDemo.client.DiscentiClient;
import com.artificial.SpringAIDemo.data.DiscenteDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IngestionDiscenti {

    private static final Logger log = LoggerFactory.getLogger(IngestionDiscenti.class);

    private final VectorStore vectorStore;
    private final DiscentiClient discentiClient;

    public IngestionDiscenti(VectorStore vectorStore, DiscentiClient discentiClient) {
        this.vectorStore = vectorStore;
        this.discentiClient = discentiClient;
    }

    @PostConstruct
    public void ingestDiscenti() {
        log.info("Inizio ingestion Discenti dal microservizio...");
        List<DiscenteDTO> discenti = discentiClient.getAllDiscenti();
        if (discenti == null || discenti.isEmpty()) {
            log.warn("Nessun discente trovato!");
            return;
        }

        List<Document> docs = new ArrayList<>();
        for (DiscenteDTO d : discenti) {
            String testo = "%s %s is a student from %s, %d years old, with matrix %s.".formatted(
                    d.getNome(), d.getCognome(), d.getCittaResidenza(), d.getEta(), d.getMatricola()
            );

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("nome", d.getNome());
            metadata.put("cognome", d.getCognome());
            metadata.put("matricola", d.getMatricola());
            metadata.put("eta", d.getEta());
            metadata.put("city", d.getCittaResidenza());

            docs.add(new Document(testo, metadata));
        }

        vectorStore.add(docs);
        log.info("Ingestion completata! Inseriti {} discenti nel VectorStore.", docs.size());
    }
}

