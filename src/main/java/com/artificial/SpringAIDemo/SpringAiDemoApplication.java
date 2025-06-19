package com.artificial.SpringAIDemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiDemoApplication {

	public SpringAiDemoApplication(@Value("${spring.ai.ollama.model:NOT FOUND}") String modello) {
		System.out.println("MODELLO CONFIGURATO: " + modello);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringAiDemoApplication.class, args);
	}
}


