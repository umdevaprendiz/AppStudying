# 📚 AppStudying

> Plataforma de organização de estudos com interação em tempo real entre usuários.

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react&logoColor=white)

---

## 🚧 Status do projeto

**Este projeto está em desenvolvimento ativo.** Funcionalidades, estrutura de pastas e endpoints ainda estão sendo implementados e podem mudar sem aviso prévio. Não recomendado para uso em produção neste momento.

---

## 📖 Sobre o projeto

O **AppStudying** é uma aplicação full-stack construída para ajudar estudantes a organizar suas matérias e sessões de estudo, com um diferencial: permitir que usuários **interajam entre si em tempo real** — solicitando parcerias de estudo e trocando mensagens diretas, no estilo de uma rede social.

A ideia central é unir organização pessoal de estudos com um componente social — transformando o ato de estudar, muitas vezes solitário, em algo colaborativo.

---

## ✨ Funcionalidades

### Implementadas

**Conta e organização de estudos**
- [x] Cadastro e autenticação de usuários (com senha criptografada via `BCryptPasswordEncoder`)
- [x] Busca, atualização e alteração de senha de usuário
- [x] CRUD completo de matérias (`Matter`) vinculadas a um usuário, documentado via Swagger/OpenAPI
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
- [x] Todas as rotas exigem autenticação, exceto cadastro, login e a documentação Swagger
- [x] Autorização por dono do recurso em todos os endpoints sensíveis — cada usuário só age sobre os próprios dados (sessões de estudo, mensagens, solicitações, matérias, eventos da linha do tempo); a identidade vem da sessão autenticada, nunca de um parâmetro enviado pelo cliente
- [x] Assinaturas do WebSocket também são validadas: não dá para se inscrever no tópico de notificação de outro usuário
- [x] Hash da senha nunca é serializado nas respostas da API (`@JsonProperty(WRITE_ONLY)`)
- [x] Proteção contra mass assignment no cadastro de usuário e criação de matéria (o `id` enviado pelo cliente é sempre ignorado)
- [x] Testes de integração (MockMvc) cobrindo o fluxo de login, bloqueio de acesso não autenticado e bloqueio de acesso a recurso de outro usuário

**Infraestrutura**
- [x] Persistência com MySQL rodando via Docker Compose (volume nomeado, dados persistem entre reinicializações do container)
- [x] Cache habilitado (`@EnableCaching`) para consultas de matéria por id
- [x] Credenciais do banco de dados isoladas via variáveis de ambiente (`.env`), carregadas por um `EnvironmentPostProcessor` próprio
- [x] Front-end em React (Vite) completo — login, cadastro, dashboard, perfil, chat e cronômetro consumindo a API REST e o WebSocket, com proxy do Vite pro backend (mesma origem, sem dor de cabeça de CORS/cookies)
- [x] Testes unitários (JUnit 5 + Mockito) para todos os services (`UserService`, `MatterService`, `TimeLineService`, `StudySessionService`, `StudyRequestService`, `ChatService`)

### Em desenvolvimento
- [ ] CRUD de tópicos (`Topic`) vinculados a uma matéria

### Preparado para deploy
- [x] `Dockerfile` multi-stage: builda o frontend, embute o build em `src/main/resources/static` e empacota tudo num único jar/container
- [x] CI (`.github/workflows/ci.yml`): testes do backend com MySQL real, lint/build do frontend, build da imagem Docker
- [x] Proteção CSRF via cookie (`XSRF-TOKEN` / header `X-XSRF-TOKEN`), com login/registro isentos por não terem sessão ainda
- [x] CORS sem wildcard: origens liberadas via `app.cors.allowed-origins`, vazio por padrão em produção (mesma origem)
- [x] Schema do banco versionado via Flyway (`src/main/resources/db/migration`); `ddl-auto=validate` em vez de `update`
- [x] CPF marcado como `WRITE_ONLY` (mesmo tratamento da senha) — não é mais devolvido em nenhuma resposta da API
- [x] Perfil `prod` (`application-prod.properties`): cookie de sessão `secure`, CORS vazio
- [x] Actuator (`/actuator/health`) para health check da plataforma de deploy

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
| Banco de dados | MySQL |
| Cache | Spring Cache |
| Documentação de API | springdoc-openapi (Swagger UI) |
| Comunicação em tempo real | WebSocket |
| Containerização | Docker / Docker Compose |
| Build | Maven |
| Front-end | React (Vite) |
| Roteamento (front-end) | React Router |
| WebSocket (front-end) | @stomp/stompjs + sockjs-client |

---

## 🏗️ Estrutura do projeto

> ⚠️ Como o projeto está em desenvolvimento, algumas etapas podem mudar.

```
AppStudying/
├── src/main/java/com/example/AppStudying/
│   ├── AppStudyingApplication.java
│   ├── configuration/        # SecurityConfig, WebConfig (CORS), DotenvEnvironmentPostProcessor
│   ├── webSocketConfig/      # Configuração do broker STOMP (/topic, /app, endpoint /ws)
│   ├── controllers/          # UserController, MatterController, StudyRequestController,
│   │                         # StudySessionController, ChatController, TimeLineController
│   ├── services/             # Regras de negócio de cada domínio
│   ├── repository/           # Interfaces JpaRepository
│   ├── model/                # User, Matter, Topic, StudySession, StudyRequest,
│   │                         # Conversation, ChatMessage, TimeLine
│   ├── enums/                # RequestStatus, TypeStatus
│   └── dto/                  # MatterStudySummaryDTO, etc.
├── src/test/java/...         # Testes unitários (JUnit 5 + Mockito), um pacote por service
├── frontend/
│   └── src/
│       ├── api/              # http.js (fetch wrapper), api.js (chamadas REST), ws.js (WebSocket)
│       ├── components/       # ProtectedRoute, ChatModal, InboxDrawer
│       ├── context/          # AuthContext (usuário logado via localStorage)
│       └── pages/            # Login, Register, Dashboard, Profile
├── compose.yaml               # Serviço MySQL com volume nomeado
└── .env                        # Credenciais locais (fora do controle de versão)
```
