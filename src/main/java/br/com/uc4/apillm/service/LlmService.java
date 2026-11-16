package br.com.uc4.apillm.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class LlmService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public LlmService(
            RestClient.Builder restClientBuilder,
            @Value("${app.llm.base-url}") String baseUrl,
            @Value("${app.llm.api-key:}") String apiKey,
            @Value("${app.llm.model}") String model) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String perguntar(String pergunta) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmNaoConfiguradaException();
        }

        try {
            JsonNode resposta = restClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(new RequestBody(model, pergunta))
                    .retrieve()
                    .body(JsonNode.class);

            String texto = extrairTexto(resposta);
            if (texto.isBlank()) {
                throw new LlmProviderException();
            }
            return texto;
        } catch (RestClientException exception) {
            // Não repassa detalhes da resposta externa, que podem conter dados sensíveis.
            throw new LlmProviderException();
        }
    }

    private String extrairTexto(JsonNode resposta) {
        if (resposta == null) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        for (JsonNode item : resposta.path("output")) {
            if (!"message".equals(item.path("type").asText())) {
                continue;
            }
            for (JsonNode conteudo : item.path("content")) {
                if ("output_text".equals(conteudo.path("type").asText())) {
                    if (!texto.isEmpty()) {
                        texto.append('\n');
                    }
                    texto.append(conteudo.path("text").asText());
                }
            }
        }
        return texto.toString().trim();
    }

    private record RequestBody(String model, String input) {
    }
}
