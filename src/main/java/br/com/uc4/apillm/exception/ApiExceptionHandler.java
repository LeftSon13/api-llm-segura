package br.com.uc4.apillm.exception;

import br.com.uc4.apillm.service.LlmNaoConfiguradaException;
import br.com.uc4.apillm.service.LlmProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Dados inválidos.");
        return ResponseEntity.badRequest().body(new ErroResponse(mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ErroResponse("O corpo da requisição deve ser um JSON válido."));
    }

    @ExceptionHandler(LlmNaoConfiguradaException.class)
    ResponseEntity<ErroResponse> tratarLlmNaoConfigurada(LlmNaoConfiguradaException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse("Configure GROQ_API_KEY para habilitar a rota de perguntas."));
    }

    @ExceptionHandler(LlmProviderException.class)
    ResponseEntity<ErroResponse> tratarFalhaDaLlm(LlmProviderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErroResponse("O serviço de IA não respondeu corretamente. Tente novamente."));
    }

    public record ErroResponse(String erro) {
    }
}
