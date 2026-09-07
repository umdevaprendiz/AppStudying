import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();

  const [groups, setGroups] = useState([]);
  const [invites, setInvites] = useState([]);
  const [newGroupName, setNewGroupName] = useState("");
  const [toasts, setToasts] = useState([]);

  function showToast(text) {
    const id = crypto.randomUUID();
    setToasts((prev) => [...prev, { id, text }]);
    setTimeout(() => setToasts((prev) => prev.filter((toast) => toast.id !== id)), 6000);
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
        showToast(err.message || t("groups.loadError"));
      }
    }
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadGroups, loadInvites]);

  useEffect(() => {
    const client = connectGroupInviteSocket(user.id, {
      onInvite: (invite) => {
        showToast(t("groups.invitedBy", { name: invite.group?.name ?? t("common.groups") }));
        loadInvites();
      },
    });
    return () => client.deactivate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      showToast(err.message || t("groups.createError"));
    }
  }

  async function handleAcceptInvite(id) {
    try {
      await Api.aceitarConviteDeGrupo(id);
      await Promise.all([loadInvites(), loadGroups()]);
    } catch (err) {
      showToast(err.message || t("groups.acceptError"));
    }
  }

  async function handleDeclineInvite(id) {
    try {
      await Api.recusarConviteDeGrupo(id);
      await loadInvites();
    } catch (err) {
      showToast(err.message || t("groups.declineError"));
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
        <div className="brand">{t("groups.brand")}</div>
        <div className="user-info">
          <button type="button" className="secondary" onClick={() => navigate("/dashboard")}>← {t("groups.panel")}</button>
        </div>
      </header>

      <main className="groups-main">
        <div className="groups-head">
          <div>
            <h1>{t("groups.title")}</h1>
            <p>{t("groups.subtitle")}</p>
          </div>
        </div>

        <section className="panel">
          <h2>{t("groups.pendingInvites")}</h2>
          <ul className="list">
            {invites.length === 0 && <li className="empty">{t("groups.noPendingInvites")}</li>}
            {invites.map((inv) => (
              <li key={inv.id}>
                <div className="item-main">
                  <strong>{inv.group?.name ?? t("common.groups")}</strong>
                  <span>{t("groups.invitedBy", { name: inv.invitedBy?.name ?? t("common.someone") })}</span>
                </div>
                <div className="actions">
                  <button onClick={() => handleAcceptInvite(inv.id)}>{t("common.accept")}</button>
                  <button className="danger" onClick={() => handleDeclineInvite(inv.id)}>{t("common.decline")}</button>
                </div>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel" style={{ marginTop: 22 }}>
          <h2>{t("groups.myGroups")}</h2>
          <form className="inline-form" onSubmit={handleCreateGroup}>
            <input
              type="text"
              placeholder={t("groups.newGroupPlaceholder")}
              value={newGroupName}
              onChange={(e) => setNewGroupName(e.target.value)}
              required
            />
            <button type="submit">{t("groups.createGroup")}</button>
          </form>

          <div className="group-grid">
            {groups.length === 0 && <div className="empty">{t("groups.noGroupsYet")}</div>}
            {groups.map((g) => (
              <button
                type="button"
                key={g.id}
                className="group-card"
                onClick={() => navigate(`/groups/${g.id}`)}
              >
                <div className="group-card-top">
                  <h3>{g.name}</h3>
                  <span className="cap">{t("groups.membersCount", { count: g.memberCount })}</span>
                </div>
                <span className="avatar" style={{ background: avatarColor(g.ownerName) }}>
                  {initials(g.ownerName)}
                </span>
                <span className={`live-badge ${g.studyingCount === 0 ? "idle" : ""}`}>
                  <span className="pulse-dot"></span>
                  {g.studyingCount === 0 ? t("groups.noOneStudying") : t("groups.studyingNowCount", { count: g.studyingCount })}
                </span>
              </button>
            ))}
          </div>
        </section>
      </main>
    </>
  );
}
