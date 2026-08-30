import { useCallback, useEffect, useState } from "react";
import { Api } from "../api/api";

function otherUser(conversation, currentUserId) {
  return conversation.requester?.id === currentUserId ? conversation.receiver : conversation.requester;
}

export function InboxDrawer({ currentUserId, unreadCount, onOpenConversation }) {
  const [open, setOpen] = useState(false);
  const [requests, setRequests] = useState([]);
  const [conversations, setConversations] = useState([]);

  const loadInbox = useCallback(async () => {
    const [reqs, convs] = await Promise.all([
      Api.listarSolicitacoesMensagem(currentUserId),
      Api.listarConversasAceitas(currentUserId),
    ]);
    setRequests(reqs);
    setConversations(convs);
  }, [currentUserId]);

  useEffect(() => {
    if (open) loadInbox();
  }, [open, loadInbox]);

  async function handleAccept(id) {
    await Api.aceitarConversa(id);
    await loadInbox();
  }

  async function handleDecline(id) {
    await Api.recusarConversa(id);
    await loadInbox();
  }

  function handleOpenConversation(conversation) {
    setOpen(false);
    onOpenConversation(otherUser(conversation, currentUserId));
  }

  return (
    <>
      <button
        type="button"
        className="inbox-fab"
        onClick={() => setOpen(true)}
        aria-label="Mensagens"
      >
        💬
        {unreadCount > 0 && <span className="inbox-badge">{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>

      {open && (
        <div className="inbox-overlay" onClick={() => setOpen(false)}>
          <div className="inbox-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="inbox-drawer-header">
              <h2>Mensagens</h2>
              <button type="button" className="secondary" onClick={() => setOpen(false)}>Fechar</button>
            </div>

            <div className="inbox-drawer-body">
              <section>
                <h3>Solicitações de mensagem</h3>
                <ul className="list">
                  {requests.length === 0 && <li className="empty">Nenhuma solicitação pendente.</li>}
                  {requests.map((r) => (
                    <li key={r.id}>
                      <div className="item-main">
                        <strong>{r.requester?.name ?? "Usuário"}</strong>
                      </div>
                      <div className="actions">
                        <button onClick={() => handleAccept(r.id)}>Aceitar</button>
                        <button className="danger" onClick={() => handleDecline(r.id)}>Recusar</button>
                      </div>
                    </li>
                  ))}
                </ul>
              </section>

              <section>
                <h3>Conversas</h3>
                <ul className="list">
                  {conversations.length === 0 && <li className="empty">Nenhuma conversa ainda.</li>}
                  {conversations.map((c) => (
                    <li key={c.id} className="clickable-row" onClick={() => handleOpenConversation(c)}>
                      <div className="item-main">
                        <strong>{otherUser(c, currentUserId)?.name ?? "Usuário"}</strong>
                      </div>
                    </li>
                  ))}
                </ul>
              </section>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
