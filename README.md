# 📚 AppStudying

> Plataforma de organização de estudos com interação em tempo real entre usuários.

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)

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
- [x] Cadastro e autenticação de usuários (com senha criptografada via `PasswordEncoder`)
- [x] Busca, atualização e alteração de senha de usuário
- [x] Criação de matérias (`Matter`) vinculadas a um usuário

### Em desenvolvimento
- [ ] CRUD completo de matérias (listar, atualizar, deletar)
- [ ] CRUD de tópicos (`Topic`) vinculados a uma matéria
- [ ] Gerenciamento de sessões de estudo (`StudySession`)
- [ ] Linha do tempo de estudos (`TimeLine`)
- [ ] Sistema de solicitação de interação entre usuários
- [ ] Notificações em tempo real via WebSocket
- [ ] Deploy em ambiente de produção com Docker

---

## 🛠️ Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot |
| Segurança | Spring Security |
| Persistência | Spring Data JPA |
| Banco de dados | MySQL |
| Comunicação em tempo real | WebSocket |
| Containerização | Docker |
| Build | Maven |

---

## 🏗️ Estrutura do projeto

> ⚠️ Como o projeto está em desenvolvimento, algumas etapas podem mudar.

```
src/main/java/com/example/AppStudying/
├── configuration/     # Configurações gerais (PasswordEncoder, etc.)
├── dto/                # Objetos de transferência de dados
├── enums/              # Enumeradores (ex: TypeStatus)
├── model/              # Entidades JPA (Matter, StudySession, TimeLine, Topic, User)
├── repository/         # Interfaces JpaRepository
├── services/           # Regras de negócio
└── webSocketConfig/    # Configuração de WebSocket
```

---

## 🚀 Como rodar o projeto

> ⚠️ Como o projeto está em desenvolvimento, algumas etapas podem mudar.

### Pré-requisitos
- Java 21+
- Docker e Docker Compose
- Maven (ou usar o wrapper `./mvnw` incluso no projeto)

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
