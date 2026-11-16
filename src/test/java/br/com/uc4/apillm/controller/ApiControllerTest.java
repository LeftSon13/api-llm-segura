package br.com.uc4.apillm.controller;

import br.com.uc4.apillm.service.LlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.security.username=teste",
        "app.security.password=senha-de-teste"
})
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmService llmService;

    @Test
    void rotaPublicaAceitaAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/publica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Esta rota é pública."));
    }

    @Test
    void rotaProtegidaExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/protegida"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/protegida").with(httpBasic("teste", "senha-de-teste")))
                .andExpect(status().isOk());
    }

    @Test
    void rotaDePerguntaExigeAutenticacao() throws Exception {
        mockMvc.perform(post("/api/perguntar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pergunta\":\"O que é orientação a objetos?\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(llmService);
    }

    @Test
    void perguntaValidaEhEnviadaParaLlm() throws Exception {
        when(llmService.perguntar("O que é orientação a objetos?"))
                .thenReturn("É uma forma de organizar o programa em objetos.");

        mockMvc.perform(post("/api/perguntar")
                        .with(httpBasic("teste", "senha-de-teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pergunta\":\"O que é orientação a objetos?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pergunta").value("O que é orientação a objetos?"))
                .andExpect(jsonPath("$.resposta").value("É uma forma de organizar o programa em objetos."));

        verify(llmService).perguntar("O que é orientação a objetos?");
    }

    @Test
    void perguntaVaziaEhRejeitadaAntesDeChamarLlm() throws Exception {
        mockMvc.perform(post("/api/perguntar")
                        .with(httpBasic("teste", "senha-de-teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pergunta\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A pergunta não pode estar vazia."));

        verifyNoInteractions(llmService);
    }

    @Test
    void perguntaMuitoLongaEhRejeitada() throws Exception {
        String pergunta = "a".repeat(2001);
        String json = "{\"pergunta\":\"" + pergunta + "\"}";

        mockMvc.perform(post("/api/perguntar")
                        .with(httpBasic("teste", "senha-de-teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A pergunta deve ter no máximo 2000 caracteres."));

        verifyNoInteractions(llmService);
    }
}
