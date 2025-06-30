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
                You are a helpful assistant.
                Using only the provided context, answer the question below.
                Respond only with the direct answer. Do not explain, do not show your reasoning, and do not provide any additional information.
                ---------------------
                {question_answer_context}
                ---------------------
                Only provide the final, direct answer.\s
                DO NOT show reasoning, explanations, or any step-by-step logic.
                If the answer is not found in the provided context, reply: "I don't know".
                Question: {query}
                Answer:
                """);

        PromptTemplate emptyContextTemplate = new PromptTemplate("""
        No context available.
        Only answer if you are certain of the correct response.
        Otherwise reply: "I don't know".
        """);

        return ContextualQueryAugmenter.builder()
                .promptTemplate(promptTemplate)
                .emptyContextPromptTemplate(emptyContextTemplate)
                .allowEmptyContext(true)
                .build();
    }
}
