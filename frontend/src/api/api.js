import { request } from "./http";

export const Api = {
  // Users
  registrarUser(user) {
    return request("POST", "/api/users/registrarUser", { body: user });
  },
  login(email, senha) {
    return request("POST", "/api/users/login", { params: { email, senha } });
  },
  verificarEmail(token) {
    return request("GET", "/api/users/verificar-email", { params: { token } });
  },
  reenviarVerificacao(email) {
    return request("POST", "/api/users/reenviar-verificacao", { params: { email } });
  },
  logout() {
    return request("POST", "/api/users/logout");
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
  listarSugestoes(limite = 10) {
    return request("GET", "/api/users/sugestoes", { params: { limite } });
  },
  alterarSenha(id, senhaAtual, novaSenha) {
    return request("PUT", `/api/users/${id}/senha`, { params: { senhaAtual, novaSenha } });
  },
  solicitarTrocaEmail(novoEmail) {
    return request("POST", "/api/users/solicitar-troca-email", { params: { novoEmail } });
  },
  confirmarTrocaEmail(token) {
    return request("POST", "/api/users/confirmar-troca-email", { params: { token } });
  },
  solicitarExclusaoConta() {
    return request("POST", "/api/users/solicitar-exclusao");
  },
  confirmarExclusaoConta(token) {
    return request("POST", "/api/users/confirmar-exclusao", { params: { token } });
  },

  // Matters
  criarMatter(nome) {
    return request("POST", "/api/matters", { body: { nome } });
  },
  buscarMatterPorId(id) {
    return request("GET", `/api/matters/${id}`);
  },
  listarMattersPorUsuario(userId) {
    return request("GET", `/api/matters/user/${userId}`);
  },

  // Topics
  criarTopic(matterId, nome) {
    return request("POST", "/api/topics", { params: { matterId }, body: { nome } });
  },
  listarTopicsPorMatter(matterId) {
    return request("GET", `/api/topics/matter/${matterId}`);
  },
  atualizarTopic(id, novoNome, novoStatus) {
    return request("PUT", `/api/topics/${id}`, { params: { novoNome, novoStatus } });
  },
  excluirTopic(id) {
    return request("DELETE", `/api/topics/${id}`);
  },

  // Study Requests
  enviarSolicitacao(receiverId, matterId, message) {
    return request("POST", "/api/study-requests", {
      params: { receiverId, matterId, message },
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
  criarEvento(description) {
    return request("POST", "/api/TimeLine/criarEvento", { params: { description } });
  },
  listarEventosPorUsuario(userId) {
    return request("GET", "/api/TimeLine/listarUsuarios", { params: { userId } });
  },
  buscarEventoPorId(id) {
    return request("GET", `/api/TimeLine/${id}`);
  },

  // Study Sessions (cronômetro)
  iniciarSessao(matterId, topicId) {
    return request("POST", "/api/study-sessions/iniciar", { params: { matterId, topicId } });
  },
  encerrarSessao(id) {
    return request("PUT", `/api/study-sessions/${id}/encerrar`);
  },
  buscarSessaoAtiva(matterId) {
    return request("GET", "/api/study-sessions/ativa", { params: { matterId } });
  },
  listarSessoesPorUsuario(userId) {
    return request("GET", `/api/study-sessions/usuario/${userId}`);
  },
  buscarResumoEstudos(userId) {
    return request("GET", `/api/study-sessions/resumo/${userId}`);
  },

  // Chat
  enviarMensagem(receiverId, content) {
    return request("POST", "/api/chat", { params: { receiverId, content } });
  },
  listarConversa(otherUserId) {
    return request("GET", "/api/chat/conversa", { params: { userId2: otherUserId } });
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
