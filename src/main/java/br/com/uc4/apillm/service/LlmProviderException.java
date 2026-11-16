package br.com.uc4.apillm.service;

public class LlmProviderException extends RuntimeException {
    public LlmProviderException() {
        super("Não foi possível obter uma resposta da LLM.");
    }
}
