import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { Api } from "../api/api";

function otherUser(conversation, currentUserId) {
  return conversation.requester?.id === currentUserId ? conversation.receiver : conversation.requester;
}

export function InboxDrawer({ currentUserId, unreadCount, onOpenConversation }) {
  const { t } = useTranslation();
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

  function handleOpen() {
    setOpen(true);
    loadInbox();
  }

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
        onClick={handleOpen}
        aria-label={t("inbox.ariaLabel")}
      >
        💬
        {unreadCount > 0 && <span className="inbox-badge">{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>

      {open && (
        <div className="inbox-overlay" onClick={() => setOpen(false)}>
          <div className="inbox-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="inbox-drawer-header">
              <h2>{t("inbox.title")}</h2>
              <button type="button" className="secondary" onClick={() => setOpen(false)}>{t("common.close")}</button>
            </div>

            <div className="inbox-drawer-body">
              <section>
                <h3>{t("inbox.messageRequests")}</h3>
                <ul className="list">
                  {requests.length === 0 && <li className="empty">{t("inbox.noPendingRequests")}</li>}
                  {requests.map((r) => (
                    <li key={r.id}>
                      <div className="item-main">
                        <strong>{r.requester?.name ?? t("common.user")}</strong>
                      </div>
                      <div className="actions">
                        <button onClick={() => handleAccept(r.id)}>{t("common.accept")}</button>
                        <button className="danger" onClick={() => handleDecline(r.id)}>{t("common.decline")}</button>
                      </div>
                    </li>
                  ))}
                </ul>
              </section>

              <section>
                <h3>{t("inbox.conversations")}</h3>
                <ul className="list">
                  {conversations.length === 0 && <li className="empty">{t("inbox.noConversations")}</li>}
                  {conversations.map((c) => (
                    <li key={c.id} className="clickable-row" onClick={() => handleOpenConversation(c)}>
                      <div className="item-main">
                        <strong>{otherUser(c, currentUserId)?.name ?? t("common.user")}</strong>
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
