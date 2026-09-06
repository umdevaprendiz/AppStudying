import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Api } from "../api/api";
import { connectGroupChatSocket, connectGroupPresenceSocket } from "../api/ws";
import { useAuth } from "../context/auth-context";

const AVATAR_COLORS = ["#3654ff", "#ff6f91", "#2b8a3e", "#e2445c", "#8a6d1a", "#2438b0"];

function initials(name) {
  return (name ?? "?").split(" ").map((p) => p[0]).slice(0, 2).join("").toUpperCase();
}

function avatarColor(name) {
  let hash = 0;
  for (const c of name ?? "") hash = (hash * 31 + c.charCodeAt(0)) % AVATAR_COLORS.length;
  return AVATAR_COLORS[hash];
}

function elapsed(sinceISO, nowMs) {
  const diff = Math.max(0, nowMs - new Date(sinceISO).getTime());
  const total = Math.floor(diff / 1000);
  const mm = String(Math.floor(total / 60)).padStart(2, "0");
  const ss = String(total % 60).padStart(2, "0");
  return `${mm}:${ss}`;
}

export function GroupRoom() {
  const { id } = useParams();
  const groupId = Number(id);
  const { user } = useAuth();
  const navigate = useNavigate();

  const [summary, setSummary] = useState(null);
  const [presence, setPresence] = useState([]);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const [now, setNow] = useState(() => Date.now());
  const [toasts, setToasts] = useState([]);

  const [inviteOpen, setInviteOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteError, setInviteError] = useState("");
  const [pendingInvites, setPendingInvites] = useState([]);

  const bottomRef = useRef(null);

  function showToast(text) {
    const toastId = crypto.randomUUID();
    setToasts((prev) => [...prev, { id: toastId, text }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== toastId)), 6000);
  }

  const loadAll = useCallback(async () => {
    const [resumo, pres, msgs] = await Promise.all([
      Api.buscarResumoGrupo(groupId),
      Api.listarPresencaGrupo(groupId),
      Api.listarMensagensGrupo(groupId),
    ]);
    setSummary(resumo);
    setPresence(pres);
    setMessages(msgs);
  }, [groupId]);

  useEffect(() => {
    async function load() {
      try {
        await loadAll();
      } catch (err) {
        showToast(err.message || "Não foi possível carregar o grupo.");
      }
    }
    load();
  }, [loadAll]);

  useEffect(() => {
    const client = connectGroupPresenceSocket(groupId, { onPresence: setPresence });
    return () => client.deactivate();
  }, [groupId]);

  useEffect(() => {
    const client = connectGroupChatSocket(groupId, {
      onMessage: (msg) => setMessages((prev) => [...prev, msg]),
    });
    return () => client.deactivate();
  }, [groupId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useEffect(() => {
    if (!presence.some((p) => p.studying)) return;
    const interval = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(interval);
  }, [presence]);

  async function handleSend(e) {
    e.preventDefault();
    if (!text.trim()) return;
    try {
      await Api.enviarMensagemGrupo(groupId, text.trim());
      setText("");
    } catch (err) {
      showToast(err.message || "Não foi possível enviar a mensagem.");
    }
  }

  async function openInvite() {
    setInviteError("");
    setInviteEmail("");
    setInviteOpen(true);
    try {
      setPendingInvites(await Api.listarConvitesPendentesDoGrupo(groupId));
    } catch (err) {
      showToast(err.message || "Não foi possível carregar os convites pendentes.");
    }
  }

  async function handleInvite(e) {
    e.preventDefault();
    setInviteError("");
    try {
      const invitee = await Api.buscarUserPorEmail(inviteEmail.trim());
      await Api.convidarParaGrupo(groupId, invitee.id);
      setInviteEmail("");
      const [resumo, pending] = await Promise.all([
        Api.buscarResumoGrupo(groupId),
        Api.listarConvitesPendentesDoGrupo(groupId),
      ]);
      setSummary(resumo);
      setPendingInvites(pending);
    } catch (err) {
      setInviteError(err.message || "Não foi possível enviar o convite.");
    }
  }

  async function handleLeave() {
    try {
      await Api.sairDoGrupo(groupId);
      navigate("/groups");
    } catch (err) {
      showToast(err.message || "Não foi possível sair do grupo.");
    }
  }

  async function handleDelete() {
    try {
      await Api.excluirGrupo(groupId);
      navigate("/groups");
    } catch (err) {
      showToast(err.message || "Não foi possível excluir o grupo.");
    }
  }

  if (!summary) {
    return (
      <main className="groups-main">
        <p>Carregando grupo...</p>
      </main>
    );
  }

  const isOwner = summary.ownerId === user.id;
  const capPct = Math.round((summary.memberCount / 30) * 100);

  return (
    <>
      <div id="toastContainer">
        {toasts.map((t) => (
          <div className="toast" key={t.id}>{t.text}</div>
        ))}
      </div>

      <header className="topbar">
        <div className="brand">AppStudying · Grupos</div>
        <div className="user-info">
          <button type="button" className="secondary" onClick={() => navigate("/dashboard")}>Painel</button>
        </div>
      </header>

      <main className="groups-main">
        <div className="room-head">
          <div className="room-head-left">
            <button type="button" className="secondary" onClick={() => navigate("/groups")}>← Grupos</button>
            <div className="room-title">
              <h1>{summary.name}</h1>
              <span className="cap">{summary.memberCount} / 30 membros</span>
            </div>
          </div>
          <div className="room-actions">
            <button type="button" onClick={openInvite}>+ Convidar</button>
            {isOwner ? (
              <button type="button" className="danger" onClick={handleDelete}>Excluir grupo</button>
            ) : (
              <button type="button" className="danger" onClick={handleLeave}>Sair do grupo</button>
            )}
          </div>
        </div>

        <div className="room-grid">
          <section className="panel roster-panel">
            <h2>Quem está estudando agora</h2>
            <ul className="roster-list">
              {[...presence].sort((a, b) => (b.studying ? 1 : 0) - (a.studying ? 1 : 0)).map((p) => (
                <li key={p.userId} className="roster-row">
                  <span className="avatar" style={{ background: avatarColor(p.userName) }}>
                    {initials(p.userName)}
                  </span>
                  <div className="roster-info">
                    <strong>{p.userName}</strong>
                    {p.studying ? (
                      <span className="roster-status">
                        <span className="dot"></span>
                        {p.matterName}{p.topicName ? ` · ${p.topicName}` : ""}
                      </span>
                    ) : (
                      <span className="roster-status idle">
                        <span className="dot"></span>
                        Sem sessão ativa agora
                      </span>
                    )}
                  </div>
                  {p.studying && (
                    <span className="roster-timer">{elapsed(p.sessionStartedAt, now)}</span>
                  )}
                </li>
              ))}
            </ul>
          </section>

          <section className="panel group-chat-panel">
            <div className="group-chat-head">
              <h2>Chat do grupo</h2>
              <span className="badge">{summary.memberCount} pessoas</span>
            </div>
            <div className="group-chat-messages">
              {messages.length === 0 && <div className="empty">Nenhuma mensagem ainda. Diga oi!</div>}
              {messages.map((m) => (
                <div key={m.id} className={`msg-group ${m.sender?.id === user.id ? "mine" : "theirs"}`}>
                  {m.sender?.id !== user.id && <span className="msg-sender">{m.sender?.name ?? "Alguém"}</span>}
                  <div className={`chat-bubble ${m.sender?.id === user.id ? "mine" : "theirs"}`}>
                    <span>{m.content}</span>
                    <span className="chat-time">
                      {m.sentAt ? new Date(m.sentAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) : ""}
                    </span>
                  </div>
                </div>
              ))}
              <div ref={bottomRef} />
            </div>
            <form className="group-chat-input" onSubmit={handleSend}>
              <input
                type="text"
                placeholder="Escreva para o grupo..."
                value={text}
                onChange={(e) => setText(e.target.value)}
                autoComplete="off"
              />
              <button type="submit">Enviar</button>
            </form>
          </section>
        </div>
      </main>

      {inviteOpen && (
        <div className="chat-overlay" onClick={() => setInviteOpen(false)}>
          <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
            <div className="chat-modal-header">
              <strong>Convidar para {summary.name}</strong>
              <button type="button" className="secondary" onClick={() => setInviteOpen(false)}>Fechar</button>
            </div>

            <div style={{ padding: "18px 20px", display: "flex", flexDirection: "column", gap: 16 }}>
              <form className="inline-form" onSubmit={handleInvite}>
                <input
                  type="email"
                  placeholder="e-mail da pessoa"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  required
                />
                <button type="submit">Convidar</button>
              </form>
              <div className="error-msg">{inviteError}</div>

              <div>
                <div className="cap-meter-label">
                  <span>Ocupação do grupo</span>
                  <span className="count">{summary.memberCount} / 30</span>
                </div>
                <div className="cap-meter-track">
                  <div className={`cap-meter-fill ${capPct >= 80 ? "near-cap" : ""}`} style={{ width: `${capPct}%` }} />
                </div>
              </div>

              <div>
                <div className="cap-meter-label"><span>Convites aguardando resposta</span></div>
                <ul className="list">
                  {pendingInvites.length === 0 && <li className="empty">Nenhum convite aguardando resposta.</li>}
                  {pendingInvites.map((inv) => (
                    <li key={inv.id}>
                      <div className="item-main"><strong>{inv.user?.name ?? "Usuário"}</strong></div>
                      <span className="badge PENDENTE">PENDENTE</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
