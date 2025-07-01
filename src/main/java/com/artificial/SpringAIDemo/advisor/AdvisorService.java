package com.artificial.SpringAIDemo.advisor;


import com.artificial.SpringAIDemo.augmentation.LinksAppendingAdvisor;
import com.artificial.SpringAIDemo.augmentation.LinksQueryAugmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service che gestisce il flusso di conversazione AI con retrieval aumentato:
 * - Recupera documenti rilevanti tramite il vector store.
 * - Aumenta la query utente con il contesto.
 * - Genera la risposta AI e aggiunge eventuali link ai documenti citati.
 * Restituisce lo stream delle risposte generate.
 */


@Service
public class AdvisorService {

    private static final Logger log = LoggerFactory.getLogger(AdvisorService.class);

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    public AdvisorService(ChatClient.Builder chatClientBuilder,
                          @Qualifier("myVectorStore") VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> call(String userMessageContent) {

        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .build();

        QueryAugmenter queryAugmenter = new LinksQueryAugmenter().create();

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .order(1)
                .build();

        LinksAppendingAdvisor linksAppendingAdvisor = new LinksAppendingAdvisor(2);

        return this.chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor, linksAppendingAdvisor)
                .user(userMessageContent)
                .stream()
                .content();

    }

}