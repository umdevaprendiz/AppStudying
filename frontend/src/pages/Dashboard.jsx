import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { connectStudyRequestSocket, connectChatSocket, connectInboxSocket } from "../api/ws";
import { useAuth } from "../context/auth-context";
import { ChatModal } from "../components/ChatModal";
import { InboxDrawer } from "../components/InboxDrawer";
import { MatterTopics } from "../components/MatterTopics";
import { useTheme } from "../hooks/useTheme";

function notificationText(studyRequest) {
  if (studyRequest.status === "PENDENTE") {
    return `Nova solicitação de estudo de ${studyRequest.requester?.name ?? "alguém"}`;
  }
  if (studyRequest.status === "ACEITA") {
    return `${studyRequest.receiver?.name ?? "Alguém"} aceitou sua solicitação de estudo`;
  }
  if (studyRequest.status === "RECUSADA") {
    return `${studyRequest.receiver?.name ?? "Alguém"} recusou sua solicitação de estudo`;
  }
  return "Atualização em uma solicitação de estudo";
}

function formatElapsed(inicioISO, nowMs) {
  const diff = Math.max(0, nowMs - new Date(inicioISO).getTime());
  const totalSeconds = Math.floor(diff / 1000);
  const mm = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
  const ss = String(totalSeconds % 60).padStart(2, "0");
  return `${mm}:${ss}`;
}

export function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();
  const [connected, setConnected] = useState(false);
  const [toasts, setToasts] = useState([]);

  const [matters, setMatters] = useState([]);
  const [received, setReceived] = useState([]);
  const [sent, setSent] = useState([]);
  const [events, setEvents] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const [matterName, setMatterName] = useState("");
  const [eventDescription, setEventDescription] = useState("");
  const [requestForm, setRequestForm] = useState({ receiverEmail: "", matterId: "", message: "" });
  const [requestError, setRequestError] = useState("");

  const [activeSessions, setActiveSessions] = useState({});
  const [now, setNow] = useState(() => Date.now());
  const [expandedMatterId, setExpandedMatterId] = useState(null);

  const [openChatWith, setOpenChatWith] = useState(null);
  const [chatMessages, setChatMessages] = useState([]);
  const openChatWithRef = useRef(null);

  const clientRef = useRef(null);
  const chatClientRef = useRef(null);

  const loadMatters = useCallback(async () => {
    const list = await Api.listarMattersPorUsuario(user.id);
    setMatters(list);

    const entries = await Promise.all(
      list.map(async (m) => {
        const active = await Api.buscarSessaoAtiva(m.id);
        return active ? [m.id, { sessionId: active.id, inicio: active.inicio }] : null;
      })
    );
    setActiveSessions(Object.fromEntries(entries.filter(Boolean)));
  }, [user.id]);

  const loadReceived = useCallback(async () => {
    setReceived(await Api.listarRecebidas(user.id));
  }, [user.id]);

  const loadSent = useCallback(async () => {
    setSent(await Api.listarEnviadas(user.id));
  }, [user.id]);

  const loadEvents = useCallback(async () => {
    setEvents(await Api.listarEventosPorUsuario(user.id));
  }, [user.id]);

  const loadSuggestions = useCallback(async () => {
    setSuggestions(await Api.listarSugestoes(10));
  }, []);

  function showToast(text) {
    const id = crypto.randomUUID();
    setToasts((prev) => [...prev, { id, text }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 6000);
  }

  useEffect(() => {
    async function loadAll() {
      try {
        await Promise.all([loadMatters(), loadReceived(), loadSent(), loadEvents(), loadSuggestions()]);
      } catch (err) {
        showToast(err.message || "Não foi possível carregar todos os dados da página. Tente recarregar.");
      }
    }
    loadAll();
  }, [loadMatters, loadReceived, loadSent, loadEvents, loadSuggestions]);

  useEffect(() => {
    Api.contarMensagensNaoLidas(user.id).then(setUnreadCount);
  }, [user.id]);

  useEffect(() => {
    const client = connectInboxSocket(user.id, { onCount: setUnreadCount });
    return () => client.deactivate();
  }, [user.id]);

  useEffect(() => {
    const client = connectStudyRequestSocket(user.id, {
      onStatusChange: setConnected,
      onNotification: (studyRequest) => {
        showToast(notificationText(studyRequest));
        loadReceived();
        loadSent();
      },
    });
    clientRef.current = client;
    return () => client.deactivate();
  }, [user.id, loadReceived, loadSent]);

  useEffect(() => {
    openChatWithRef.current = openChatWith;
  }, [openChatWith]);

  useEffect(() => {
    const client = connectChatSocket(user.id, {
      onMessage: (msg) => {
        const partner = openChatWithRef.current;
        if (!partner) return;
        const otherId = msg.sender?.id === user.id ? msg.receiver?.id : msg.sender?.id;
        if (otherId === partner.id) {
          setChatMessages((prev) => [...prev, msg]);
        }
      },
    });
    chatClientRef.current = client;
    return () => client.deactivate();
  }, [user.id]);

  useEffect(() => {
    if (Object.keys(activeSessions).length === 0) return;
    const interval = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(interval);
  }, [activeSessions]);

  async function handleCreateMatter(e) {
    e.preventDefault();
    if (!matterName.trim()) return;
    await Api.criarMatter(matterName.trim());
    setMatterName("");
    await loadMatters();
  }

  async function handleCreateEvent(e) {
    e.preventDefault();
    if (!eventDescription.trim()) return;
    await Api.criarEvento(eventDescription.trim());
    setEventDescription("");
    await loadEvents();
  }

  async function handleSendRequest(e) {
    e.preventDefault();
    setRequestError("");
    try {
      const receiver = await Api.buscarUserPorEmail(requestForm.receiverEmail.trim());
      await Api.enviarSolicitacao(
        receiver.id,
        requestForm.matterId || undefined,
        requestForm.message.trim() || undefined
      );
      setRequestForm({ receiverEmail: "", matterId: "", message: "" });
      await loadSent();
    } catch (err) {
      setRequestError(err.message || "Não foi possível enviar a solicitação.");
    }
  }

  async function respond(requestId, accept) {
    if (accept) {
      await Api.aceitarSolicitacao(requestId);
    } else {
      await Api.recusarSolicitacao(requestId);
    }
    await loadReceived();
  }

  async function handleStartTimer(matterId) {
    const session = await Api.iniciarSessao(matterId);
    setActiveSessions((prev) => ({ ...prev, [matterId]: { sessionId: session.id, inicio: session.inicio } }));
  }

  async function handleStopTimer(matterId) {
    const active = activeSessions[matterId];
    if (!active) return;
    await Api.encerrarSessao(active.sessionId);
    setActiveSessions((prev) => {
      const next = { ...prev };
      delete next[matterId];
      return next;
    });
    await loadEvents();
  }

  async function openChat(partner) {
    if (!partner?.id) return;
    setOpenChatWith(partner);
    setChatMessages(await Api.listarConversa(partner.id));
  }

  function closeChat() {
    setOpenChatWith(null);
    setChatMessages([]);
  }

  async function handleSendMessage(text) {
    if (!openChatWith) return;
    try {
      await Api.enviarMensagem(openChatWith.id, text);
    } catch (err) {
      showToast(err.message || "Não foi possível enviar a mensagem.");
    }
  }

  async function handleLogout() {
    try {
      await Api.logout();
    } finally {
      logout();
    }
  }

  return (
    <>
      <div id="toastContainer">
        {toasts.map((t) => (
          <div className="toast" key={t.id}>{t.text}</div>
        ))}
      </div>

      <header className="topbar">
        <div className="brand">AppStudying</div>
        <div className="user-info">
          <span>{user.name} ({user.email})</span>
          <span className={`ws-status ${connected ? "connected" : ""}`}>
            {connected ? "conectado" : "conectando..."}
          </span>
          <button
            type="button"
            className="secondary"
            onClick={toggleTheme}
            aria-label={theme === "dark" ? "Ativar tema claro" : "Ativar tema escuro"}
          >
            {theme === "dark" ? "Tema claro" : "Tema escuro"}
          </button>
          <button className="secondary" onClick={() => navigate("/settings")}>Configurações</button>
          <button className="secondary" onClick={handleLogout}>Sair</button>
        </div>
      </header>

      <main>
        <section className="panel">
          <h2>Minhas matérias</h2>
          <form className="inline-form" onSubmit={handleCreateMatter}>
            <input
              type="text"
              placeholder="Nome da matéria"
              value={matterName}
              onChange={(e) => setMatterName(e.target.value)}
              required
            />
            <button type="submit">Adicionar</button>
          </form>
          <ul className="list">
            {matters.length === 0 && <li className="empty">Nada por aqui ainda.</li>}
            {matters.map((m) => {
              const active = activeSessions[m.id];
              const expanded = expandedMatterId === m.id;
              return (
                <li key={m.id} className="matter-item">
                  <div className="item-main"><strong>{m.nome}</strong></div>
                  <div className="actions">
                    {active ? (
                      <>
                        <span className="timer-display">{formatElapsed(active.inicio, now)}</span>
                        <button className="danger" onClick={() => handleStopTimer(m.id)}>Parar</button>
                      </>
                    ) : (
                      <button onClick={() => handleStartTimer(m.id)}>Iniciar cronômetro</button>
                    )}
                    <button
                      className="secondary"
                      onClick={() => setExpandedMatterId(expanded ? null : m.id)}
                    >
                      {expanded ? "Ocultar tópicos" : "Tópicos"}
                    </button>
                  </div>
                  {expanded && <MatterTopics matterId={m.id} />}
                </li>
              );
            })}
          </ul>
        </section>

        <section className="panel wide">
          <h2>Pessoas para conhecer</h2>
          <div className="people-grid">
            {suggestions.length === 0 && <div className="empty">Nenhuma sugestão por enquanto.</div>}
            {suggestions.map((s) => (
              <button
                type="button"
                key={s.id}
                className="people-card"
                onClick={() => navigate(`/profile/${s.id}`)}
              >
                <span className="people-avatar">{s.name?.[0]?.toUpperCase() ?? "?"}</span>
                <span>{s.name}</span>
              </button>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Enviar solicitação de estudo</h2>
          <form className="inline-form" style={{ flexDirection: "column", alignItems: "stretch" }} onSubmit={handleSendRequest}>
            <label htmlFor="receiverEmail">E-mail do destinatário</label>
            <input
              id="receiverEmail"
              type="email"
              value={requestForm.receiverEmail}
              onChange={(e) => setRequestForm((p) => ({ ...p, receiverEmail: e.target.value }))}
              required
            />

            <label htmlFor="matterSelect">Matéria (opcional)</label>
            <select
              id="matterSelect"
              value={requestForm.matterId}
              onChange={(e) => setRequestForm((p) => ({ ...p, matterId: e.target.value }))}
            >
              <option value="">Nenhuma</option>
              {matters.map((m) => (
                <option key={m.id} value={m.id}>{m.nome}</option>
              ))}
            </select>

            <label htmlFor="requestMessage">Mensagem (opcional)</label>
            <textarea
              id="requestMessage"
              rows={2}
              value={requestForm.message}
              onChange={(e) => setRequestForm((p) => ({ ...p, message: e.target.value }))}
            />

            <button type="submit" className="full-width">Enviar solicitação</button>
            <div className="error-msg">{requestError}</div>
          </form>
        </section>

        <section className="panel wide">
          <h2>Solicitações recebidas</h2>
          <ul className="list">
            {received.length === 0 && <li className="empty">Nada por aqui ainda.</li>}
            {received.map((r) => (
              <li key={r.id}>
                <div className="item-main">
                  {r.requester?.id ? (
                    <button type="button" className="clickable-name" onClick={() => openChat(r.requester)}>
                      {r.requester.name}
                    </button>
                  ) : (
                    <strong>Usuário</strong>
                  )}
                  <span>{r.message ?? ""}</span>
                </div>
                <span className={`badge ${r.status}`}>{r.status}</span>
                {r.status === "PENDENTE" && (
                  <div className="actions">
                    <button onClick={() => respond(r.id, true)}>Aceitar</button>
                    <button className="danger" onClick={() => respond(r.id, false)}>Recusar</button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        </section>

        <section className="panel wide">
          <h2>Solicitações enviadas</h2>
          <ul className="list">
            {sent.length === 0 && <li className="empty">Nada por aqui ainda.</li>}
            {sent.map((r) => (
              <li key={r.id}>
                <div className="item-main">
                  {r.receiver?.id ? (
                    <button type="button" className="clickable-name" onClick={() => openChat(r.receiver)}>
                      Para: {r.receiver.name}
                    </button>
                  ) : (
                    <strong>Para: Usuário</strong>
                  )}
                  <span>{r.message ?? ""}</span>
                </div>
                <span className={`badge ${r.status}`}>{r.status}</span>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel wide">
          <h2>Linha do tempo</h2>
          <form className="inline-form" onSubmit={handleCreateEvent}>
            <input
              type="text"
              placeholder="Descrição do evento"
              value={eventDescription}
              onChange={(e) => setEventDescription(e.target.value)}
              required
            />
            <button type="submit">Adicionar evento</button>
          </form>
          <ul className="list">
            {events.length === 0 && <li className="empty">Nada por aqui ainda.</li>}
            {events.map((ev) => (
              <li key={ev.id}>
                <div className="item-main">
                  <strong>{ev.description}</strong>
                  <span>{ev.eventDate ? new Date(ev.eventDate).toLocaleString("pt-BR") : ""}</span>
                </div>
              </li>
            ))}
          </ul>
        </section>
      </main>

      <ChatModal
        partner={openChatWith}
        currentUserId={user.id}
        messages={chatMessages}
        onClose={closeChat}
        onSend={handleSendMessage}
      />

      <InboxDrawer
        currentUserId={user.id}
        unreadCount={unreadCount}
        onOpenConversation={openChat}
      />
    </>
  );
}
