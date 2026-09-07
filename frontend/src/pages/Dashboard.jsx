import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Api } from "../api/api";
import { connectStudyRequestSocket, connectChatSocket, connectInboxSocket } from "../api/ws";
import { useAuth } from "../context/auth-context";
import { ChatModal } from "../components/ChatModal";
import { InboxDrawer } from "../components/InboxDrawer";
import { MatterTopics } from "../components/MatterTopics";
import { LanguageSwitcher } from "../components/LanguageSwitcher";
import { useTheme } from "../hooks/useTheme";
import { dateLocale } from "../i18n/dateLocale";

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
  const { t } = useTranslation();
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

  function notificationText(studyRequest) {
    if (studyRequest.status === "PENDENTE") {
      return t("dashboard.notification.pending", { name: studyRequest.requester?.name ?? t("common.someone") });
    }
    if (studyRequest.status === "ACEITA") {
      return t("dashboard.notification.accepted", { name: studyRequest.receiver?.name ?? t("common.someone") });
    }
    if (studyRequest.status === "RECUSADA") {
      return t("dashboard.notification.declined", { name: studyRequest.receiver?.name ?? t("common.someone") });
    }
    return t("dashboard.notification.generic");
  }

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
    setTimeout(() => setToasts((prev) => prev.filter((toast) => toast.id !== id)), 6000);
  }

  useEffect(() => {
    async function loadAll() {
      try {
        await Promise.all([loadMatters(), loadReceived(), loadSent(), loadEvents(), loadSuggestions()]);
      } catch (err) {
        showToast(err.message || t("dashboard.loadPageError"));
      }
    }
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      setRequestError(err.message || t("dashboard.sendRequestError"));
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
      showToast(err.message || t("dashboard.sendMessageError"));
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
        {toasts.map((toast) => (
          <div className="toast" key={toast.id}>{toast.text}</div>
        ))}
      </div>

      <header className="topbar">
        <div className="brand">{t("common.appName")}</div>
        <div className="user-info">
          <span>{user.name} ({user.email})</span>
          <span className={`ws-status ${connected ? "connected" : ""}`}>
            {connected ? t("common.connected") : t("common.connecting")}
          </span>
          <LanguageSwitcher />
          <button
            type="button"
            className="secondary"
            onClick={toggleTheme}
            aria-label={theme === "dark" ? t("common.lightTheme") : t("common.darkTheme")}
          >
            {theme === "dark" ? t("common.lightTheme") : t("common.darkTheme")}
          </button>
          <button className="secondary" onClick={() => navigate("/groups")}>{t("common.groups")}</button>
          <button className="secondary" onClick={() => navigate("/settings")}>{t("common.settings")}</button>
          <button className="secondary" onClick={handleLogout}>{t("common.logout")}</button>
        </div>
      </header>

      <main>
        <section className="panel">
          <h2>{t("dashboard.myMatters")}</h2>
          <form className="inline-form" onSubmit={handleCreateMatter}>
            <input
              type="text"
              placeholder={t("dashboard.matterNamePlaceholder")}
              value={matterName}
              onChange={(e) => setMatterName(e.target.value)}
              required
            />
            <button type="submit">{t("common.add")}</button>
          </form>
          <ul className="list">
            {matters.length === 0 && <li className="empty">{t("dashboard.nothingHere")}</li>}
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
                        <button className="danger" onClick={() => handleStopTimer(m.id)}>{t("dashboard.stop")}</button>
                      </>
                    ) : (
                      <button onClick={() => handleStartTimer(m.id)}>{t("dashboard.startTimer")}</button>
                    )}
                    <button
                      className="secondary"
                      onClick={() => setExpandedMatterId(expanded ? null : m.id)}
                    >
                      {expanded ? t("dashboard.hideTopics") : t("dashboard.topics")}
                    </button>
                  </div>
                  {expanded && <MatterTopics matterId={m.id} />}
                </li>
              );
            })}
          </ul>
        </section>

        <section className="panel wide">
          <h2>{t("dashboard.peopleToMeet")}</h2>
          <div className="people-grid">
            {suggestions.length === 0 && <div className="empty">{t("dashboard.noSuggestions")}</div>}
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
          <h2>{t("dashboard.sendStudyRequest")}</h2>
          <form className="inline-form" style={{ flexDirection: "column", alignItems: "stretch" }} onSubmit={handleSendRequest}>
            <label htmlFor="receiverEmail">{t("dashboard.recipientEmail")}</label>
            <input
              id="receiverEmail"
              type="email"
              value={requestForm.receiverEmail}
              onChange={(e) => setRequestForm((p) => ({ ...p, receiverEmail: e.target.value }))}
              required
            />

            <label htmlFor="matterSelect">{t("dashboard.matterOptional")}</label>
            <select
              id="matterSelect"
              value={requestForm.matterId}
              onChange={(e) => setRequestForm((p) => ({ ...p, matterId: e.target.value }))}
            >
              <option value="">{t("dashboard.none")}</option>
              {matters.map((m) => (
                <option key={m.id} value={m.id}>{m.nome}</option>
              ))}
            </select>

            <label htmlFor="requestMessage">{t("dashboard.messageOptional")}</label>
            <textarea
              id="requestMessage"
              rows={2}
              value={requestForm.message}
              onChange={(e) => setRequestForm((p) => ({ ...p, message: e.target.value }))}
            />

            <button type="submit" className="full-width">{t("dashboard.sendRequestButton")}</button>
            <div className="error-msg">{requestError}</div>
          </form>
        </section>

        <section className="panel wide">
          <h2>{t("dashboard.requestsReceived")}</h2>
          <ul className="list">
            {received.length === 0 && <li className="empty">{t("dashboard.nothingHere")}</li>}
            {received.map((r) => (
              <li key={r.id}>
                <div className="item-main">
                  {r.requester?.id ? (
                    <button type="button" className="clickable-name" onClick={() => openChat(r.requester)}>
                      {r.requester.name}
                    </button>
                  ) : (
                    <strong>{t("common.user")}</strong>
                  )}
                  <span>{r.message ?? ""}</span>
                </div>
                <span className={`badge ${r.status}`}>{t(`status.${r.status}`, r.status)}</span>
                {r.status === "PENDENTE" && (
                  <div className="actions">
                    <button onClick={() => respond(r.id, true)}>{t("common.accept")}</button>
                    <button className="danger" onClick={() => respond(r.id, false)}>{t("common.decline")}</button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        </section>

        <section className="panel wide">
          <h2>{t("dashboard.requestsSent")}</h2>
          <ul className="list">
            {sent.length === 0 && <li className="empty">{t("dashboard.nothingHere")}</li>}
            {sent.map((r) => (
              <li key={r.id}>
                <div className="item-main">
                  {r.receiver?.id ? (
                    <button type="button" className="clickable-name" onClick={() => openChat(r.receiver)}>
                      {t("dashboard.to")}: {r.receiver.name}
                    </button>
                  ) : (
                    <strong>{t("dashboard.to")}: {t("common.user")}</strong>
                  )}
                  <span>{r.message ?? ""}</span>
                </div>
                <span className={`badge ${r.status}`}>{t(`status.${r.status}`, r.status)}</span>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel wide">
          <h2>{t("dashboard.timeline")}</h2>
          <form className="inline-form" onSubmit={handleCreateEvent}>
            <input
              type="text"
              placeholder={t("dashboard.eventDescriptionPlaceholder")}
              value={eventDescription}
              onChange={(e) => setEventDescription(e.target.value)}
              required
            />
            <button type="submit">{t("dashboard.addEvent")}</button>
          </form>
          <ul className="list">
            {events.length === 0 && <li className="empty">{t("dashboard.nothingHere")}</li>}
            {events.map((ev) => (
              <li key={ev.id}>
                <div className="item-main">
                  <strong>{ev.description}</strong>
                  <span>{ev.eventDate ? new Date(ev.eventDate).toLocaleString(dateLocale()) : ""}</span>
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
