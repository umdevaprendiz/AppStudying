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

O **AppStudying** é uma aplicação backend construída para ajudar estudantes a organizar suas matérias, tópicos e sessões de estudo, com um diferencial: permitir que usuários **interajam entre si em tempo real**, solicitando parcerias de estudo através de WebSocket.

A ideia central é unir organização pessoal de estudos com um componente social — transformando o ato de estudar, muitas vezes solitário, em algo colaborativo.

---

## ✨ Funcionalidades

### Implementadas
- [x] Cadastro e autenticação de usuários (com senha criptografada via `BCryptPasswordEncoder`)
- [x] Busca, atualização e alteração de senha de usuário
- [x] Criação de matérias (`Matter`) vinculadas a um usuário
- [x] API REST para matérias (`MatterController`) documentada via Swagger/OpenAPI
- [x] Persistência com MySQL rodando via Docker Compose
- [x] Cache habilitado (`@EnableCaching`) para consultas de matéria por id
- [x] Credenciais do banco de dados isoladas via variáveis de ambiente (`.env`)
- [x] API REST de usuários (`UserController`) — registro, busca por id/email, login e atualização
- [x] CRUD completo de matérias (atualizar, deletar)
- [x] Registro de eventos em linha do tempo (`TimeLineController`) — criação e listagem por usuário
- [x] Sistema de solicitação de interação entre usuários (`StudyRequest`) — enviar, aceitar e recusar solicitações de estudo
- [x] Notificações em tempo real via WebSocket (STOMP + SockJS) quando uma solicitação é enviada, aceita ou recusada
- [x] Testes unitários (JUnit 5 + Mockito) para os services (`UserService`, `MatterService`, `TimeLineService`, `StudySessionService`, `StudyRequestService`)

### Em desenvolvimento
- [ ] CRUD de tópicos (`Topic`) vinculados a uma matéria
- [ ] Gerenciamento de sessões de estudo (`StudySession`)
- [ ] Regras de autorização do Spring Security (atualmente todas as rotas estão liberadas para facilitar o desenvolvimento do CRUD)
- [ ] Front-end em React (estrutura inicial já criada em `/frontend`)
- [ ] Deploy em ambiente de produção com Docker

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

---

## 🏗️ Estrutura do projeto

> ⚠️ Como o projeto está em desenvolvimento, algumas etapas podem mudar.
