# 📚 AppStudying

> Plataforma de organização de estudos com interação em tempo real entre usuários.

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

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

### Em desenvolvimento
- [ ] API REST de usuários (`UserController`)
- [ ] CRUD completo de matérias (atualizar, deletar)
- [ ] CRUD de tópicos (`Topic`) vinculados a uma matéria
- [ ] Gerenciamento de sessões de estudo (`StudySession`)
- [ ] Linha do tempo de estudos (`TimeLine`)
- [ ] Sistema de solicitação de interação entre usuários
- [ ] Notificações em tempo real via WebSocket
- [ ] Regras de autorização do Spring Security (atualmente todas as rotas estão liberadas para facilitar o desenvolvimento do CRUD)
- [ ] Testes unitários (JUnit 5 + Mockito) para os services
- [ ] Front-end em React (estrutura inicial já criada em `/frontend`)
- [ ] Deploy em ambiente de produção com Docker

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

> ⚠️ Como o projeto está em desenvolvimento, algumas etapas podem mudar.)

### Passos

```bash
# Clone o repositório
git clone https://github.com/umdevaprendiz/AppStudying.git
cd AppStudying

# Suba a aplicação e o banco de dados via Docker Compose
docker compose up -d --build

# Acompanhe os logs da aplicação
docker compose logs -f app
```

## 🗺️ Roadmap

1. Finalizar CRUD de matérias e tópicos
2. Implementar sessões de estudo com cálculo de duração
3. Construir agregação de dados na `TimeLine`
4. Implementar sistema de solicitação de interação entre usuários
5. Integrar notificações em tempo real via WebSocket
6. Criar tratamento centralizado de exceções (`@ControllerAdvice`)
7. Escrever testes automatizados
8. Deploy em ambiente de produção

---

## 👤 Autor

Desenvolvido por [**Sérgio Guilherme**](https://github.com/umdevaprendiz) como projeto de portfólio, aliado ao aprendizado prático de Spring Boot, arquitetura em camadas e comunicação em tempo real.

---


Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.
