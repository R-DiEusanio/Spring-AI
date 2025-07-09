package com.artificial.SpringAIDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiDemoApplication {

	public SpringAiDemoApplication(@Value("${spring.ai.openai.chat.model:NOT FOUND}") String modello) {
		System.out.println("MODELLO CONFIGURATO: " + modello);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringAiDemoApplication.class, args);
	}
}
