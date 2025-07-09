package com.artificial.SpringAIDemo.loader;

import com.artificial.SpringAIDemo.client.DiscentiClient;
import com.artificial.SpringAIDemo.data.DiscenteDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.*;

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

        discenti.sort(Comparator.comparing(DiscenteDTO::getCognome, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(DiscenteDTO::getNome, String.CASE_INSENSITIVE_ORDER));

        List<Document> documents = new ArrayList<>();

        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.9)
                .topK(3)
                .build();

        StringBuilder builder = new StringBuilder("""
This document contains the full list of all students currently in the system.
Each student is listed with their full details: name, age, city of residence, and matricola.
Each student is listed on a separate line with their name, age, city of residence, and matricola.
Use this data to answer questions like "list all students" or "show all registered students".

Ordered list:
""");

        for (DiscenteDTO d : discenti) {
            String matricola = d.getMatricola();

            boolean esistente = retriever.retrieve(new org.springframework.ai.rag.Query(matricola)).stream()
                    .anyMatch(doc -> matricola.equals(doc.getMetadata().get("matricola")));

            if (esistente) {
                log.info("Documento già presente per matricola: {}", matricola);
                continue;
            }

            builder.append(String.format("""
• %s %s
  Age: %d
  City of Residence: %s
  Matricola: %s

""", d.getNome(), d.getCognome(), d.getEta(), d.getCittaResidenza(), d.getMatricola()));

            String testo = "%s %s is a %d-year-old student from %s, with matrix %s.".formatted(
                    d.getNome(), d.getCognome(), d.getEta(), d.getCittaResidenza(), d.getMatricola());

            Map<String, Object> meta = new HashMap<>();
            meta.put("nome", d.getNome());
            meta.put("cognome", d.getCognome());
            meta.put("matricola", d.getMatricola());
            meta.put("eta", d.getEta());
            meta.put("città", d.getCittaResidenza());
            meta.put("tipo", "studente_singolo");

            documents.add(new Document(testo, meta));
        }

        // Verifica se esiste già la lista ordinata completa
        boolean listaEsistente = retriever.retrieve(new org.springframework.ai.rag.Query("lista_studenti_ordinata")).stream()
                .anyMatch(doc -> "lista_studenti_ordinata".equals(doc.getMetadata().get("tipo")));

        if (!listaEsistente) {
            Map<String, Object> metaLista = new HashMap<>();
            metaLista.put("tipo", "lista_studenti_ordinata");
            metaLista.put("categoria", "studenti");
            metaLista.put("formato", "puntato");
            metaLista.put("ordinato", true);

            documents.add(new Document(builder.toString(), metaLista));
            log.info("Aggiunto documento lista ordinata studenti.");
        } else {
            log.info("Lista ordinata già presente, non reinserita.");
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("Ingestion completata: {} nuovi documenti salvati.", documents.size());
        } else {
            log.info("Nessun nuovo documento da inserire.");
        }
    }

}
