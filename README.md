# API de LLM com Spring Boot e segurança

Projeto da atividade de reposição da UC4 — Back End de Projetos Web. A API oferece uma rota pública, uma rota autenticada e uma rota autenticada que envia uma pergunta para a API Responses da Groq. O plano gratuito tem limites de uso; consulte as cotas atuais da sua conta antes dos testes. A documentação da Groq confirma compatibilidade do endpoint Responses usado nesta aplicação.

Para apresentar a implementação ao professor, consulte [docs/EXPLICACAO_DO_PROJETO.md](docs/EXPLICACAO_DO_PROJETO.md) e o [diagrama do fluxo](docs/FLUXO_DA_REQUISICAO.md).

## Requisitos

- Java 21 ou 25. A aplicação e os testes foram executados com essas duas versões.
- O Maven Wrapper incluído baixa e usa o Maven sem exigir uma instalação global.
- Uma chave da API Groq para receber respostas reais da LLM. Crie-a no [console oficial da Groq](https://console.groq.com/keys).

## Executar no Windows

No PowerShell, dentro da pasta do projeto. O `JAVA_HOME` desta máquina já aponta para o JDK 21; para usar o JDK 25, aponte-o para a pasta dessa instalação.

```powershell
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:GROQ_API_KEY = "sua-chave-aqui"
$env:APP_USER = "aluno"
$env:APP_PASSWORD = "escolha-uma-senha-forte"
.\mvnw.cmd spring-boot:run
```

Se não configurar `GROQ_API_KEY`, a aplicação inicia normalmente, mas `POST /api/perguntar` responde `503 Service Unavailable` informando que falta configurar a chave. A chave nunca deve ser escrita no código, no `application.properties`, compartilhada em chats ou enviada ao Git. A chave fica configurada apenas no ambiente local.

## Rotas

| Método e rota | Acesso | Comportamento |
|---|---|---|
| `GET /api/publica` | Público | Retorna uma mensagem pública. |
| `GET /api/protegida` | HTTP Basic | Retorna uma mensagem após autenticação. |
| `POST /api/perguntar` | HTTP Basic | Valida a pergunta e consulta a LLM. |

As credenciais são lidas de `APP_USER` e `APP_PASSWORD`. A senha é armazenada em memória e codificada com BCrypt; essa configuração é didática e não substitui um provedor de identidade para produção.

Corpo de `POST /api/perguntar`:

```json
{
  "pergunta": "Explique de forma simples o que é orientação a objetos."
}
```

A pergunta precisa conter texto não vazio e pode ter até 2.000 caracteres. Erros de validação retornam HTTP 400, falta de chave HTTP 503 e falha do provedor HTTP 502. A integração usa o modelo configurado por `LLM_MODEL` (padrão: `openai/gpt-oss-20b`); `LLM_BASE_URL` pode apontar para outro provedor compatível com o endpoint `/v1/responses`.

Exemplos de chamadas:

```powershell
curl.exe http://localhost:8080/api/publica
curl.exe -u "${env:APP_USER}:$env:APP_PASSWORD" http://localhost:8080/api/protegida
curl.exe -u "${env:APP_USER}:$env:APP_PASSWORD" -H "Content-Type: application/json" -d '{"pergunta":"O que é orientação a objetos?"}' http://localhost:8080/api/perguntar
```

## Testes

Os testes cobrem acesso público, autenticação, proteção da rota de IA, resposta da rota de perguntas e validação de pergunta vazia ou longa. O cliente da LLM é simulado nos testes, então eles não consomem créditos nem precisam de uma chave real.

```powershell
.\mvnw.cmd test
```

## Respostas às questões

1. **Quais dados não deveriam ser enviados para uma LLM?** Senhas, chaves de API, tokens de sessão, dados pessoais ou financeiros, prontuários, informações confidenciais de clientes/empresas e qualquer conteúdo sem autorização ou necessidade. Dados devem ser minimizados e, quando possível, anonimizados.
2. **Qual o risco de colocar a chave da API diretamente no código?** Ela pode ser exposta em repositórios, histórico de versões, logs ou cópias do projeto. Alguém pode usá-la para consumir a cota e gerar custos ou acessar recursos da conta. Se exposta, deve ser revogada e substituída.
3. **Por que uma rota que utiliza IA deve possuir controle de acesso?** Para limitar quem pode enviar dados ao provedor e consumir recursos pagos, reduzir abuso e manter rastreabilidade. A autenticação também ajuda a aplicar limites por usuário.
4. **Qual a diferença entre autenticação e autorização?** Autenticação confirma quem está fazendo a requisição; autorização decide o que essa identidade pode acessar ou executar.
