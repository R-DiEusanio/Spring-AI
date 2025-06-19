package com.artificial.SpringAIDemo.augmentation;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;


/**
 * Classe factory per creare un ContextualQueryAugmenter configurato con prompt personalizzati.
 * Serve per aumentare una query includendo informazioni di contesto:
 * - Se c'è contesto, la risposta sarà basata su di esso.
 * - Se non c'è contesto, la risposta invita l'utente a seguire i link forniti.
 * Utile per sistemi RAG (Retrieval Augmented Generation) e chatbot documentali.
 */


public class LinksQueryAugmenter {
    public ContextualQueryAugmenter create() {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Context information is below.
                ---------------------
                {question_answer_context}
                ---------------------
                Given the context and no prior knowledge, answer the query.
                If the answer is not in the context, respond: "Follow links provided below."
                Query: {query}
                Answer:
                """);

        PromptTemplate emptyContextTemplate = new PromptTemplate("""
                Given the context and no prior knowledge, answer the query.
                If the answer is not in the context, respond: "Follow links provided below."
        """);

        return ContextualQueryAugmenter.builder()
                .promptTemplate(promptTemplate)
                .emptyContextPromptTemplate(emptyContextTemplate)
                .allowEmptyContext(true)
                .build();
    }
}
