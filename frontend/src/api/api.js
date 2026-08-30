import { request } from "./http";

export const Api = {
  // Users
  registrarUser(user) {
    return request("POST", "/api/users/registrarUser", { body: user });
  },
  login(email, senha) {
    return request("POST", "/api/users/login", { params: { email, senha } });
  },
  buscarUserPorEmail(email) {
    return request("GET", `/api/users/buscarUser/${encodeURIComponent(email)}`);
  },
  buscarUserPorId(id) {
    return request("GET", `/api/users/${id}`);
  },
  atualizarUsuario(id, novoNome, email) {
    return request("PUT", `/api/users/${id}`, { params: { novoNome, email } });
  },
  listarSugestoes(userId, limite = 10) {
    return request("GET", "/api/users/sugestoes", { params: { userId, limite } });
  },

  // Matters
  criarMatter(nome, userId) {
    return request("POST", "/api/matters", { params: { userId }, body: { nome } });
  },
  buscarMatterPorId(id) {
    return request("GET", `/api/matters/${id}`);
  },
  listarMattersPorUsuario(userId) {
    return request("GET", `/api/matters/user/${userId}`);
  },

  // Study Requests
  enviarSolicitacao(requesterId, receiverId, matterId, message) {
    return request("POST", "/api/study-requests", {
      params: { requesterId, receiverId, matterId, message },
    });
  },
  aceitarSolicitacao(id) {
    return request("PUT", `/api/study-requests/${id}/aceitar`);
  },
  recusarSolicitacao(id) {
    return request("PUT", `/api/study-requests/${id}/recusar`);
  },
  listarRecebidas(userId) {
    return request("GET", `/api/study-requests/recebidas/${userId}`);
  },
  listarEnviadas(userId) {
    return request("GET", `/api/study-requests/enviadas/${userId}`);
  },
  buscarSolicitacaoPorId(id) {
    return request("GET", `/api/study-requests/${id}`);
  },

  // Timeline
  criarEvento(description, userId) {
    return request("POST", "/api/TimeLine/criarEvento", { params: { description, userId } });
  },
  listarEventosPorUsuario(userId) {
    return request("GET", "/api/TimeLine/listarUsuarios", { params: { userId } });
  },
  buscarEventoPorId(id) {
    return request("GET", `/api/TimeLine/${id}`);
  },

  // Study Sessions (cronômetro)
  iniciarSessao(userId, matterId, topicId) {
    return request("POST", "/api/study-sessions/iniciar", { params: { userId, matterId, topicId } });
  },
  encerrarSessao(id) {
    return request("PUT", `/api/study-sessions/${id}/encerrar`);
  },
  buscarSessaoAtiva(userId, matterId) {
    return request("GET", "/api/study-sessions/ativa", { params: { userId, matterId } });
  },
  listarSessoesPorUsuario(userId) {
    return request("GET", `/api/study-sessions/usuario/${userId}`);
  },
  buscarResumoEstudos(userId) {
    return request("GET", `/api/study-sessions/resumo/${userId}`);
  },

  // Chat
  enviarMensagem(senderId, receiverId, content) {
    return request("POST", "/api/chat", { params: { senderId, receiverId, content } });
  },
  listarConversa(userId1, userId2) {
    return request("GET", "/api/chat/conversa", { params: { userId1, userId2 } });
  },
  listarSolicitacoesMensagem(userId) {
    return request("GET", `/api/chat/solicitacoes/${userId}`);
  },
  listarConversasAceitas(userId) {
    return request("GET", `/api/chat/conversas/${userId}`);
  },
  aceitarConversa(id) {
    return request("PUT", `/api/chat/conversas/${id}/aceitar`);
  },
  recusarConversa(id) {
    return request("PUT", `/api/chat/conversas/${id}/recusar`);
  },
  contarMensagensNaoLidas(userId) {
    return request("GET", `/api/chat/contagem/${userId}`);
  },
};
