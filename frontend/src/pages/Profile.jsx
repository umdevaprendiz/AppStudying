import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Api } from "../api/api";
import { connectChatSocket } from "../api/ws";
import { useAuth } from "../context/auth-context";
import { ChatModal } from "../components/ChatModal";

function formatMinutes(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours === 0) return `${minutes} min`;
  return `${hours}h ${minutes}min`;
}

export function Profile() {
  const { userId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [profile, setProfile] = useState(null);
  const [summary, setSummary] = useState([]);
  const [chatOpen, setChatOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState([]);
  const chatOpenRef = useRef(false);
  const clientRef = useRef(null);

  const profileId = Number(userId);

  useEffect(() => {
    async function load() {
      const [profileData, summaryData] = await Promise.all([
        Api.buscarUserPorId(profileId),
        Api.buscarResumoEstudos(profileId),
      ]);
      setProfile(profileData);
      setSummary(summaryData);
    }
    load();
  }, [profileId]);

  useEffect(() => {
    chatOpenRef.current = chatOpen;
  }, [chatOpen]);

  useEffect(() => {
    const client = connectChatSocket(user.id, {
      onMessage: (msg) => {
        if (!chatOpenRef.current) return;
        const otherId = msg.sender?.id === user.id ? msg.receiver?.id : msg.sender?.id;
        if (otherId === profileId) {
          setChatMessages((prev) => [...prev, msg]);
        }
      },
    });
    clientRef.current = client;
    return () => client.deactivate();
  }, [user.id, profileId]);

  async function openChat() {
    setChatOpen(true);
    setChatMessages(await Api.listarConversa(user.id, profileId));
  }

  async function handleSendMessage(text) {
    try {
      await Api.enviarMensagem(user.id, profileId, text);
    } catch (err) {
      window.alert(err.message || "Não foi possível enviar a mensagem.");
    }
  }

  if (!profile) {
    return <div className="auth-wrapper"><div className="card">Carregando perfil...</div></div>;
  }

  return (
    <>
      <header className="topbar">
        <div className="brand">AppStudying</div>
        <button className="secondary" onClick={() => navigate(-1)}>Voltar</button>
      </header>

      <main className="profile-main">
        <section className="panel wide profile-header">
          <div className="profile-avatar">{profile.name?.[0]?.toUpperCase() ?? "?"}</div>
          <div>
            <h2>{profile.name}</h2>
            <span className="item-main"><span>{profile.email}</span></span>
          </div>
          {user.id !== profileId && (
            <button className="full-width profile-message-btn" onClick={openChat}>Mandar mensagem</button>
          )}
        </section>

        <section className="panel wide">
          <h2>Matérias estudadas</h2>
          <ul className="list">
            {summary.length === 0 && <li className="empty">Nenhuma sessão de estudo registrada ainda.</li>}
            {summary.map((s) => (
              <li key={s.matterName}>
                <div className="item-main">
                  <strong>{s.matterName}</strong>
                  <span>{s.totalSessions} sessão(ões)</span>
                </div>
                <span className="timer-display">{formatMinutes(s.totalMinutes)}</span>
              </li>
            ))}
          </ul>
        </section>
      </main>

      {chatOpen && (
        <ChatModal
          partner={profile}
          currentUserId={user.id}
          messages={chatMessages}
          onClose={() => setChatOpen(false)}
          onSend={handleSendMessage}
        />
      )}
    </>
  );
}
