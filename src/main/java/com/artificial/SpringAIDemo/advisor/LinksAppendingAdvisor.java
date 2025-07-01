package com.artificial.SpringAIDemo.augmentation;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advisor che aggiunge i link dei documenti utilizzati come footer nella risposta dell'AI.
 * Se non ci sono documenti, restituisce la risposta originale senza footer.
 */


public class LinksAppendingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LinksAppendingAdvisor.class);
    private int order = 0;

    public LinksAppendingAdvisor() {
    }

    public LinksAppendingAdvisor(int order) {
        this.order = order;
    }

    @Nonnull
    @Override
    public AdvisedRequest before(@Nonnull AdvisedRequest request) {
        return request;
    }

    @Nonnull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {
        ChatResponse.Builder chatResponseBuilder;

        if (advisedResponse.response() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(advisedResponse.response());

            List<Generation> generations = new ArrayList<>(advisedResponse.response().getResults());
            if (!generations.isEmpty()) {
                Generation generation = generations.get(generations.size() - 1);

                String linksFooter = getLinksFooter(advisedResponse);

                log.info("Adding links footer: {}", linksFooter);
                generations.set(generations.size() - 1,
                        new Generation(
                                new AssistantMessage(generation.getOutput().getText() + linksFooter),
                                generation.getMetadata()
                        ));
                chatResponseBuilder.generations(generations);
            }
        }
        return new AdvisedResponse(chatResponseBuilder.build(), advisedResponse.adviseContext());
    }

    private String getLinksFooter(AdvisedResponse advisedResponse) {
        List<?> documents = (List<?>) advisedResponse
                .adviseContext()
                .get("rag_document_context");

        if (documents == null || documents.isEmpty()) {
            return "";
        }

        Map<String, Set<Integer>> filePathToPagesMap = documents.stream()
                .filter(Objects::nonNull)
                .map(doc -> (Document) doc)
                .collect(Collectors.groupingBy(
                        doc -> String.valueOf(doc.getMetadata().get("file_path")),
                        Collectors.mapping(
                                doc -> {
                                    Object pageNum = doc.getMetadata().get("page_number");
                                    return pageNum instanceof Integer ? (Integer) pageNum : 0;
                                },
                                Collectors.toCollection(TreeSet::new)
                        )
                ));

        StringBuilder footer = new StringBuilder();
        footer.append("\n\n---------------------------------------\nLinks:");

        filePathToPagesMap.forEach((filePath, pages) -> {
            footer.append("\n ").append(filePath).append(", pages: ")
                    .append(pages.stream().map(String::valueOf).collect(Collectors.joining(",")));
        });

        return footer.toString();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}