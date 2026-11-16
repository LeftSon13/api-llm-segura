package br.com.uc4.apillm.service;

public class LlmNaoConfiguradaException extends RuntimeException {
    public LlmNaoConfiguradaException() {
        super("A chave da API da LLM não foi configurada.");
    }
}
