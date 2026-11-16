package br.com.uc4.apillm.controller;

import br.com.uc4.apillm.dto.PerguntaRequest;
import br.com.uc4.apillm.dto.RespostaLlmResponse;
import br.com.uc4.apillm.service.LlmService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final LlmService llmService;

    public ApiController(LlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/publica")
    public MensagemResponse rotaPublica() {
        return new MensagemResponse("Esta rota é pública.");
    }

    @GetMapping("/protegida")
    public MensagemResponse rotaProtegida() {
        return new MensagemResponse("Acesso autorizado à rota protegida.");
    }

    @PostMapping("/perguntar")
    public RespostaLlmResponse perguntar(@Valid @RequestBody PerguntaRequest request) {
        String resposta = llmService.perguntar(request.pergunta());
        return new RespostaLlmResponse(request.pergunta(), resposta);
    }

    public record MensagemResponse(String mensagem) {
    }
}
