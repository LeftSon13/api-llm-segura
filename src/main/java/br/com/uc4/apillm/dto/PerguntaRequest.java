package br.com.uc4.apillm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerguntaRequest(
        @NotBlank(message = "A pergunta não pode estar vazia.")
        @Size(max = 2000, message = "A pergunta deve ter no máximo 2000 caracteres.")
        String pergunta) {
}
