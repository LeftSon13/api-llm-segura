# Explicação do projeto — API com LLM, APIs e Segurança

## 1. Objetivo

O projeto atende à atividade de reposição da UC4 — Back End de Projetos Web. Foi desenvolvida uma API REST em Java com Spring Boot que demonstra rotas públicas e protegidas, validação de entrada e integração com um serviço de linguagem (LLM).

## 2. Rotas implementadas

| Método e rota | Acesso | Resultado |
|---|---|---|
| `GET /api/publica` | Público | Retorna uma mensagem sem exigir autenticação. |
| `GET /api/protegida` | Autenticado | Retorna uma mensagem após autenticação HTTP Basic. |
| `POST /api/perguntar` | Autenticado | Valida uma pergunta, consulta a Groq e retorna a pergunta junto com a resposta gerada. |

Exemplo do corpo enviado à rota de IA:

```json
{
  "pergunta": "Explique de forma simples o que é orientação a objetos."
}
```

Exemplo do formato de resposta:

```json
{
  "pergunta": "Explique de forma simples o que é orientação a objetos.",
  "resposta": "Texto produzido pela LLM."
}
```

## 3. Como a requisição percorre a aplicação

1. O cliente envia uma requisição HTTP.
2. O Spring Security verifica a rota. `GET /api/publica` é permitido sem autenticação; `/api/protegida` e `/api/perguntar` exigem HTTP Basic.
3. O controlador encaminha a requisição para o método correspondente.
4. Em `POST /api/perguntar`, o Spring Validation verifica o campo `pergunta` antes de qualquer chamada externa.
5. Se a pergunta for válida, o serviço verifica se `GROQ_API_KEY` foi configurada e envia a pergunta à API Responses da Groq.
6. A resposta textual do provedor é extraída e devolvida ao cliente como JSON.

O diagrama completo está em [FLUXO_DA_REQUISICAO.md](FLUXO_DA_REQUISICAO.md).

## 4. Organização do código

| Arquivo | Responsabilidade |
|---|---|
| `src/main/java/br/com/uc4/apillm/ApiLlmSeguraApplication.java` | Inicializa a aplicação Spring Boot. |
| `src/main/java/br/com/uc4/apillm/config/SecurityConfig.java` | Define as rotas públicas e protegidas, HTTP Basic, usuário em memória e codificação BCrypt da senha. |
| `src/main/java/br/com/uc4/apillm/controller/ApiController.java` | Implementa as três rotas HTTP. |
| `src/main/java/br/com/uc4/apillm/dto/PerguntaRequest.java` | Define e valida os dados de entrada. |
| `src/main/java/br/com/uc4/apillm/service/LlmService.java` | Faz a chamada HTTP à LLM e extrai o texto da resposta. |
| `src/main/java/br/com/uc4/apillm/exception/ApiExceptionHandler.java` | Converte erros de validação e de integração em respostas HTTP adequadas. |
| `src/main/resources/application.properties` | Lê credenciais, chave, modelo e endereço do provedor a partir de variáveis de ambiente, com valores padrão não secretos para o provedor e o modelo. |
| `src/test/java/br/com/uc4/apillm/controller/ApiControllerTest.java` | Testa rotas, autenticação e validação; simula a LLM para não depender de chamadas externas. |

## 5. Segurança e validação

- A rota pública é explicitamente liberada; as outras duas rotas exigem autenticação.
- O usuário e a senha da aplicação vêm das variáveis `APP_USER` e `APP_PASSWORD`.
- A senha é codificada com BCrypt antes de ser usada pelo Spring Security.
- A chave do provedor vem de `GROQ_API_KEY`; ela não está incluída no código-fonte nem em arquivos versionados.
- A pergunta não pode estar vazia e pode ter no máximo 2.000 caracteres.
- Uma requisição não autenticada recebe HTTP `401`; dados inválidos recebem `400`; chave ausente recebe `503`; falha do provedor recebe `502`.

O usuário é mantido em memória e esta autenticação serve para a demonstração didática. Em uma aplicação de produção, seriam necessários HTTPS, gerenciamento de identidade e políticas adicionais de acesso e uso.

## 6. Integração com a LLM

A aplicação usa a API Responses compatível da Groq. O endereço padrão é `https://api.groq.com/openai/v1`, e o modelo padrão é `openai/gpt-oss-20b`. Ambos podem ser substituídos pelas variáveis `LLM_BASE_URL` e `LLM_MODEL`.

A chave é enviada ao provedor somente pelo cabeçalho de autorização da chamada HTTP. Se a chave não estiver configurada, a aplicação inicia normalmente, mas a rota de pergunta responde HTTP `503` sem tentar enviar a solicitação.

## 7. Como executar no Windows

No terminal integrado do VS Code, aberto na pasta raiz do projeto, configure as variáveis e inicie a aplicação:

```powershell
$env:APP_USER = "usuario-de-teste"
$env:APP_PASSWORD = "senha-de-teste"
$env:GROQ_API_KEY = "chave-gerada-no-console-da-Groq"
.\mvnw.cmd spring-boot:run
```

Deixe esse terminal aberto enquanto a aplicação estiver rodando. Use outro terminal para enviar as requisições. Não inicie uma segunda instância na porta `8080`; isso causa conflito de porta.

Para executar os testes automatizados:

```powershell
.\mvnw.cmd test
```

## 8. Testes e validação realizados

Os seis testes automatizados passaram, sem falhas. Eles verificam:

- acesso público sem credenciais;
- bloqueio da rota protegida sem autenticação e acesso com autenticação;
- bloqueio da rota de IA sem autenticação;
- envio de uma pergunta válida ao serviço simulado e formato da resposta;
- rejeição de pergunta vazia e de pergunta acima do limite.

Os testes não consomem créditos, pois o serviço da LLM é simulado. Também foi feita uma chamada manual autenticada à Groq, que retornou HTTP `200` com uma resposta válida.

## 9. Respostas às questões da atividade

### 1. Quais dados não deveriam ser enviados para uma LLM?

Senhas, chaves de API, tokens, dados pessoais ou financeiros e informações confidenciais de pessoas, clientes ou empresas. A entrada deve ser minimizada e, quando possível, anonimizada.

### 2. Qual o risco de colocar a chave da API diretamente no código?

A chave pode vazar pelo repositório, histórico de versões, logs ou cópias do projeto. Terceiros podem usá-la, consumir a cota ou acessar recursos associados à conta. Por isso, ela é fornecida por variável de ambiente.

### 3. Por que uma rota que utiliza IA deve possuir controle de acesso?

Para limitar quem pode enviar dados ao provedor, reduzir abuso e controlar o consumo de recursos. A autenticação também permite associar as requisições a usuários.

### 4. Qual a diferença entre autenticação e autorização?

Autenticação confirma a identidade de quem fez a requisição. Autorização determina o que essa identidade pode acessar ou executar.
