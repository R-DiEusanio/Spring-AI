package com.artificial.SpringAIDemo.client;

import com.artificial.SpringAIDemo.data.DiscenteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class DiscentiClient {

    private final WebClient webClient;

    @Autowired
    public DiscentiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8085")
                .build();
    }

    public DiscentiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<DiscenteDTO> getAllDiscenti() {
        return webClient.get()
                .uri("/discenti/lista")
                .retrieve()
                .bodyToFlux(DiscenteDTO.class)
                .collectList()
                .block();
    }

    public List<DiscenteDTO> getDiscentiByCitta(String citta) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discenti")
                        .queryParam("citta", citta)
                        .build())
                .retrieve()
                .bodyToFlux(DiscenteDTO.class)
                .collectList()
                .block();
    }

    public DiscenteDTO getDiscenteById(Long id) {
        return webClient.get()
                .uri("/discenti/{id}", id)
                .retrieve()
                .bodyToMono(DiscenteDTO.class)
                .block();
    }

    public DiscenteDTO createDiscente(DiscenteDTO discente) {
        return webClient.post()
                .uri("/discenti/create")
                .bodyValue(discente)
                .retrieve()
                .bodyToMono(DiscenteDTO.class)
                .block();
    }

    public DiscenteDTO updateDiscente(String matricola, DiscenteDTO updated) {
        return webClient.put()
                .uri("/discenti/matricola/{matricola}", matricola)
                .bodyValue(updated)
                .retrieve()
                .bodyToMono(DiscenteDTO.class)
                .block();
    }

    public void deleteDiscente(String matricola) {
        webClient.delete()
                .uri("/discenti/matricola/{matricola}", matricola)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


}
