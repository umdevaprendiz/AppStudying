# 📚 AppStudying

> Plataforma de organização de estudos com interação em tempo real entre usuários.

![Status](https://img.shields.io/badge/status-ativo-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## 🚧 Status do projeto

**No ar:** [appstudying.onrender.com](https://appstudying.onrender.com) — hospedado no Render (free tier: pode levar ~50s pra "acordar" no primeiro acesso após um período sem uso) com banco MySQL gerenciado no Aiven.

Projeto completo e funcional: autenticação com verificação de e-mail, autorização, tempo real, configurações de conta (troca de senha, exclusão com confirmação por e-mail e expurgo automático de contas inativas), testes automatizados e pipeline de deploy (Docker, CI, migrations versionadas via Flyway).

---

## 📖 Sobre o projeto

O **AppStudying** é uma aplicação full-stack construída para ajudar estudantes a organizar suas matérias e sessões de estudo, com um diferencial: permitir que usuários **interajam entre si em tempo real** — solicitando parcerias de estudo e trocando mensagens diretas, no estilo de uma rede social.

A ideia central é unir organização pessoal de estudos com um componente social — transformando o ato de estudar, muitas vezes solitário, em algo colaborativo.

---

## ✨ Funcionalidades

### Implementadas

**Conta e organização de estudos**
- [x] Cadastro e autenticação de usuários (com senha criptografada via `BCryptPasswordEncoder`)
- [x] Confirmação de e-mail obrigatória no cadastro (token com validade, e-mail via API da Brevo) — login fica bloqueado até a conta ser verificada, com opção de reenviar o e-mail
- [x] Página de configurações: troca de senha e exclusão da própria conta
- [x] Exclusão de conta protegida por confirmação em e-mail (mesmo padrão do cadastro) — a conta só é apagada depois que a pessoa clica no link, nunca automaticamente ao abrir a página
- [x] Expurgo automático diário de contas inativas há 30+ dias (job agendado), usando o mesmo fluxo de exclusão em cascata
- [x] Busca, atualização e alteração de senha de usuário
- [x] CRUD completo de matérias (`Matter`) e de tópicos (`Topic`) vinculados a uma matéria, documentado via Swagger/OpenAPI
- [x] Cronômetro de sessão de estudo por matéria (`StudySessionController`) — inicia, para, calcula a duração e salva automaticamente um evento na linha do tempo
- [x] Registro de eventos em linha do tempo/calendário (`TimeLineController`) — criação e listagem por usuário
- [x] Resumo de matérias estudadas por usuário (tempo total e nº de sessões), usado na página de perfil

**Rede social entre estudantes**
- [x] Sistema de solicitação de parceria de estudo (`StudyRequest`) — enviar, aceitar e recusar
- [x] Feed de sugestão de outros usuários para conhecer (`/api/users/sugestoes`), sem precisar saber o e-mail de ninguém
- [x] Perfil público de usuário (`/profile/:id`) mostrando as matérias estudadas
- [x] Chat privado entre usuários com histórico persistido no banco
- [x] Mensagens funcionam como solicitação (estilo "Direct"): a primeira mensagem para alguém fica pendente até a pessoa aceitar (ou simplesmente responder)
- [x] Ícone de mensagens com contador de não lidos em tempo real, abrindo um painel com solicitações recebidas e conversas ativas
- [x] Notificações em tempo real via WebSocket (STOMP + SockJS) para solicitações de estudo, solicitações de mensagem e novas mensagens

**Segurança**
- [x] Autenticação real via Spring Security com sessão persistida em banco (`spring-session-jdbc`) — login estabelece uma sessão, cookie `SESSION` autentica as próximas requisições
- [x] Todas as rotas exigem autenticação, exceto cadastro, login, verificação/exclusão de conta por token e a documentação Swagger
- [x] Autorização por dono do recurso em todos os endpoints sensíveis — cada usuário só age sobre os próprios dados (sessões de estudo, mensagens, solicitações, matérias, tópicos, eventos da linha do tempo); a identidade vem da sessão autenticada, nunca de um parâmetro enviado pelo cliente
- [x] Assinaturas do WebSocket também são validadas: não dá para se inscrever no tópico de notificação de outro usuário
- [x] Limite de tentativas (rate limiting) em login, reenvio de verificação de e-mail e solicitação de exclusão de conta — evita força bruta de senha e spam de e-mail
- [x] Hash da senha nunca é serializado nas respostas da API (`@JsonProperty(WRITE_ONLY)`)
- [x] Proteção contra mass assignment no cadastro de usuário e criação de matéria (o `id` enviado pelo cliente é sempre ignorado)
- [x] Testes de integração (MockMvc) cobrindo o fluxo de login, bloqueio de acesso não autenticado e bloqueio de acesso a recurso de outro usuário

**Infraestrutura**
- [x] Persistência com MySQL rodando via Docker Compose em dev (volume nomeado) e MySQL gerenciado (Aiven) em produção, com conexão criptografada (SSL obrigatório)
- [x] Cache habilitado (`@EnableCaching`) para consultas de matéria por id
- [x] Credenciais do banco de dados e da API de e-mail isoladas via variáveis de ambiente (`.env`), carregadas por um `EnvironmentPostProcessor` próprio
- [x] Envio de e-mail transacional via API HTTP da Brevo (não SMTP direto — a maioria dos provedores de hospedagem grátis bloqueia portas SMTP de saída)
- [x] Fuso horário do servidor fixado explicitamente no boot da aplicação, pra `LocalDateTime.now()` bater com o horário real independente de onde o container está hospedado
- [x] Front-end em React (Vite) completo — login, cadastro, verificação de e-mail, dashboard, tópicos, configurações, perfil, chat e cronômetro consumindo a API REST e o WebSocket, com proxy do Vite pro backend (mesma origem, sem dor de cabeça de CORS/cookies)
- [x] Testes unitários (JUnit 5 + Mockito) para todos os services e agendamentos (`UserService`, `MatterService`, `TopicService`, `TimeLineService`, `StudySessionService`, `StudyRequestService`, `ChatService`, `AccountDeletionService`, `AccountCleanupScheduler`)

### Preparado para deploy
- [x] `Dockerfile` multi-stage: builda o frontend, embute o build em `src/main/resources/static` e empacota tudo num único jar/container
- [x] CI (`.github/workflows/ci.yml`): testes do backend com MySQL real, lint/build do frontend, build da imagem Docker
- [x] Proteção CSRF via cookie (`XSRF-TOKEN` / header `X-XSRF-TOKEN`), com login/registro/exclusão-de-conta-por-token isentos por não terem sessão ainda
- [x] CORS sem wildcard: origens liberadas via `app.cors.allowed-origins`, vazio por padrão em produção (mesma origem)
- [x] Schema do banco versionado via Flyway (`src/main/resources/db/migration`); `ddl-auto=validate` em vez de `update`
- [x] Cadastro coleta apenas nome, e-mail e senha — nenhum dado sensível como CPF é armazenado
- [x] Perfil `prod` (`application-prod.properties`): cookie de sessão `secure`, CORS vazio, pool de conexões do Hikari ajustado pra um banco remoto
- [x] Actuator (`/actuator/health`) para health check da plataforma de deploy

---

## 🚀 Como rodar localmente

```bash
git clone https://github.com/umdevaprendiz/AppStudying.git
cd AppStudying
cp .env.example .env   # preencha com suas próprias credenciais locais
```

**Opção 1 — tudo em Docker:**
```bash
docker compose up -d          # sobe o MySQL
docker build -t appstudying . && docker run --env-file .env -p 8080:8080 appstudying
```

**Opção 2 — backend e frontend separados (hot reload):**
```bash
docker compose up -d          # sobe só o MySQL
./mvnw spring-boot:run        # backend em :8080

cd frontend
npm install
npm run dev                   # frontend em :5173, com proxy pro backend
```

A API fica documentada em `http://localhost:8080/swagger-ui.html`.

---

## 🖼️ Screenshots

> Documentação interativa da API via Swagger UI, rodando em `http://localhost:8080/swagger-ui.html`.

*(Prints em breve)*

---

## 🛠️ Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Segurança | Spring Security |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | MySQL (Aiven em produção) |
| Migrations | Flyway |
| Cache | Spring Cache |
| Agendamento | Spring Scheduling (`@Scheduled`) |
| E-mail transacional | API HTTP da Brevo |
| Documentação de API | springdoc-openapi (Swagger UI) |
| Comunicação em tempo real | WebSocket |
| Containerização | Docker / Docker Compose |
| Build | Maven |
| Deploy | Render |
| Front-end | React (Vite) |
| Roteamento (front-end) | React Router |
| WebSocket (front-end) | @stomp/stompjs + sockjs-client |

---

## 🏗️ Estrutura do projeto

```
AppStudying/
├── src/main/java/com/example/AppStudying/
│   ├── AppStudyingApplication.java   # @EnableCaching, @EnableScheduling, fuso horário fixo
│   ├── configuration/        # SecurityConfig, WebConfig (CORS), FlywayConfig,
│   │                         # DotenvEnvironmentPostProcessor
│   ├── webSocketConfig/      # Configuração do broker STOMP (/topic, /app, endpoint /ws)
│   ├── controllers/          # UserController, MatterController, TopicController,
│   │                         # StudyRequestController, StudySessionController, ChatController,
│   │                         # TimeLineController, CsrfController, SpaForwardController
│   ├── services/             # Regras de negócio de cada domínio, incluindo EmailService
│   │                         # (Brevo), AccountDeletionService (exclusão em cascata) e
│   │                         # AccountCleanupScheduler (expurgo diário de contas inativas)
│   ├── security/              # CurrentUser, CustomUserDetails(Service), RateLimiterService
│   ├── repository/           # Interfaces JpaRepository
│   ├── model/                # User, Matter, Topic, StudySession, StudyRequest,
│   │                         # Conversation, ChatMessage, TimeLine
│   ├── enums/                # RequestStatus, TypeStatus
│   └── dto/                  # MatterStudySummaryDTO, etc.
├── src/main/resources/
│   ├── application.properties, application-prod.properties
│   └── db/migration/         # Migrations versionadas (Flyway, V1 a V4)
├── src/test/java/...         # Testes unitários e de integração (JUnit 5 + Mockito + MockMvc)
├── frontend/
│   └── src/
│       ├── api/              # http.js (fetch wrapper + CSRF), api.js (chamadas REST), ws.js (WebSocket)
│       ├── components/       # ProtectedRoute, ChatModal, InboxDrawer, MatterTopics
│       ├── context/          # AuthContext (usuário logado via localStorage)
│       └── pages/            # Login, Register, VerifyEmail, Dashboard, Profile,
│                              # Settings, DeleteAccount
├── .github/workflows/ci.yml  # CI: testes com MySQL real, lint/build do frontend, build da imagem
├── Dockerfile                 # Build multi-stage: frontend + backend num único container
├── compose.yaml               # Serviço MySQL com volume nomeado (uso local/CI)
└── .env.example                # Template de variáveis de ambiente (copie para .env)
```

---

## 📄 Licença

Distribuído sob a licença MIT — veja [LICENSE](LICENSE) para mais detalhes.
