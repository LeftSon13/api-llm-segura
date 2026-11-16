# Fluxo da requisição

Este diagrama acompanha o caminho desde a chamada HTTP até a resposta da API ou da Groq.

```mermaid
flowchart TD
    A[Cliente envia requisição HTTP] --> B[Spring Security verifica método e rota]
    B -->|GET /api/publica| C[ApiController retorna mensagem pública]
    B -->|GET /api/protegida ou POST /api/perguntar| D{HTTP Basic válido?}
    D -->|Não| E[401 Não autenticado]
    D -->|Sim| F[ApiController recebe a requisição]
    F -->|GET /api/protegida| G[Retorna mensagem protegida]
    F -->|POST /api/perguntar| H[Valida PerguntaRequest]
    H -->|Vazia ou acima de 2.000 caracteres| I[400 Dados inválidos]
    H -->|Válida| J[LlmService verifica GROQ_API_KEY]
    J -->|Chave ausente| K[503 Serviço de IA não configurado]
    J -->|Chave presente| L[POST /responses para a Groq]
    L -->|Falha do provedor| M[502 Erro ao consultar IA]
    L -->|Resposta recebida| N[Extrai o texto da resposta]
    N --> O[200 JSON com pergunta e resposta]
```

## Onde cada etapa acontece

| Etapa | Arquivo |
|---|---|
| Regras públicas e protegidas | `src/main/java/br/com/uc4/apillm/config/SecurityConfig.java` |
| Rotas HTTP | `src/main/java/br/com/uc4/apillm/controller/ApiController.java` |
| Validação da pergunta | `src/main/java/br/com/uc4/apillm/dto/PerguntaRequest.java` |
| Chamada HTTP à Groq e leitura da resposta | `src/main/java/br/com/uc4/apillm/service/LlmService.java` |
| Conversão de erros em respostas HTTP | `src/main/java/br/com/uc4/apillm/exception/ApiExceptionHandler.java` |

## Exemplo da rota de IA

Requisição `POST /api/perguntar` autenticada:

```json
{
  "pergunta": "O que é orientação a objetos?"
}
```

Resposta esperada:

```json
{
  "pergunta": "O que é orientação a objetos?",
  "resposta": "...texto gerado pela LLM..."
}
```
