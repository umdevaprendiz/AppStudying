import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { connectGroupInviteSocket } from "../api/ws";
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

export function Groups() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [groups, setGroups] = useState([]);
  const [invites, setInvites] = useState([]);
  const [newGroupName, setNewGroupName] = useState("");
  const [toasts, setToasts] = useState([]);

  function showToast(text) {
    const id = crypto.randomUUID();
    setToasts((prev) => [...prev, { id, text }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 6000);
  }

  const loadGroups = useCallback(async () => {
    setGroups(await Api.listarMeusGrupos(user.id));
  }, [user.id]);

  const loadInvites = useCallback(async () => {
    setInvites(await Api.listarConvitesDeGrupo(user.id));
  }, [user.id]);

  useEffect(() => {
    async function loadAll() {
      try {
        await Promise.all([loadGroups(), loadInvites()]);
      } catch (err) {
        showToast(err.message || "Não foi possível carregar seus grupos.");
      }
    }
    loadAll();
  }, [loadGroups, loadInvites]);

  useEffect(() => {
    const client = connectGroupInviteSocket(user.id, {
      onInvite: (invite) => {
        showToast(`Você foi convidado para o grupo "${invite.group?.name ?? "um grupo"}"`);
        loadInvites();
      },
    });
    return () => client.deactivate();
  }, [user.id, loadInvites]);

  async function handleCreateGroup(e) {
    e.preventDefault();
    if (!newGroupName.trim()) return;
    try {
      const group = await Api.criarGrupo(newGroupName.trim());
      setNewGroupName("");
      await loadGroups();
      navigate(`/groups/${group.id}`);
    } catch (err) {
      showToast(err.message || "Não foi possível criar o grupo.");
    }
  }

  async function handleAcceptInvite(id) {
    try {
      await Api.aceitarConviteDeGrupo(id);
      await Promise.all([loadInvites(), loadGroups()]);
    } catch (err) {
      showToast(err.message || "Não foi possível aceitar o convite.");
    }
  }

  async function handleDeclineInvite(id) {
    try {
      await Api.recusarConviteDeGrupo(id);
      await loadInvites();
    } catch (err) {
      showToast(err.message || "Não foi possível recusar o convite.");
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
        <div className="brand">AppStudying · Grupos</div>
        <div className="user-info">
          <button type="button" className="secondary" onClick={() => navigate("/dashboard")}>← Painel</button>
        </div>
      </header>

      <main className="groups-main">
        <div className="groups-head">
          <div>
            <h1>Seus grupos de estudo</h1>
            <p>Crie uma sala, convide até 30 pessoas e veja o que cada uma está estudando agora.</p>
          </div>
        </div>

        <section className="panel">
          <h2>Convites pendentes</h2>
          <ul className="list">
            {invites.length === 0 && <li className="empty">Nenhum convite de grupo por enquanto.</li>}
            {invites.map((inv) => (
              <li key={inv.id}>
                <div className="item-main">
                  <strong>{inv.group?.name ?? "Grupo"}</strong>
                  <span>Convidado por {inv.invitedBy?.name ?? "alguém"}</span>
                </div>
                <div className="actions">
                  <button onClick={() => handleAcceptInvite(inv.id)}>Aceitar</button>
                  <button className="danger" onClick={() => handleDeclineInvite(inv.id)}>Recusar</button>
                </div>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel" style={{ marginTop: 22 }}>
          <h2>Meus grupos</h2>
          <form className="inline-form" onSubmit={handleCreateGroup}>
            <input
              type="text"
              placeholder="Nome do novo grupo (ex: Cálculo Squad)"
              value={newGroupName}
              onChange={(e) => setNewGroupName(e.target.value)}
              required
            />
            <button type="submit">Criar grupo</button>
          </form>

          <div className="group-grid">
            {groups.length === 0 && <div className="empty">Você ainda não faz parte de nenhum grupo.</div>}
            {groups.map((g) => (
              <button
                type="button"
                key={g.id}
                className="group-card"
                onClick={() => navigate(`/groups/${g.id}`)}
              >
                <div className="group-card-top">
                  <h3>{g.name}</h3>
                  <span className="cap">{g.memberCount}/30</span>
                </div>
                <span className="avatar" style={{ background: avatarColor(g.ownerName) }}>
                  {initials(g.ownerName)}
                </span>
                <span className={`live-badge ${g.studyingCount === 0 ? "idle" : ""}`}>
                  <span className="pulse-dot"></span>
                  {g.studyingCount === 0 ? "Ninguém estudando agora" : `${g.studyingCount} estudando agora`}
                </span>
              </button>
            ))}
          </div>
        </section>
      </main>
    </>
  );
}
