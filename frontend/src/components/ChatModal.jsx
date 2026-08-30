import { useEffect, useRef, useState } from "react";

export function ChatModal({ partner, currentUserId, messages, onClose, onSend }) {
  const [text, setText] = useState("");
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  if (!partner) return null;

  function handleSubmit(e) {
    e.preventDefault();
    if (!text.trim()) return;
    onSend(text.trim());
    setText("");
  }

  return (
    <div className="chat-overlay" onClick={onClose}>
      <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
        <div className="chat-modal-header">
          <strong>{partner.name}</strong>
          <button type="button" className="secondary" onClick={onClose}>Fechar</button>
        </div>

        <div className="chat-modal-messages">
          {messages.length === 0 && <div className="empty">Nenhuma mensagem ainda. Diga oi!</div>}
          {messages.map((m) => (
            <div key={m.id} className={`chat-bubble ${m.sender?.id === currentUserId ? "mine" : "theirs"}`}>
              <span>{m.content}</span>
              <span className="chat-time">
                {m.sentAt ? new Date(m.sentAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) : ""}
              </span>
            </div>
          ))}
          <div ref={bottomRef} />
        </div>

        <form className="chat-modal-input" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Digite uma mensagem..."
            value={text}
            onChange={(e) => setText(e.target.value)}
            autoFocus
          />
          <button type="submit">Enviar</button>
        </form>
      </div>
    </div>
  );
}
