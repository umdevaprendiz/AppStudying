import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Api } from "../api/api";

export function ConfirmEmailChange() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [status, setStatus] = useState(() => (token ? "confirmando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : "Link inválido: token não informado."));

  // Mesmo padrão da exclusão de conta: troca de e-mail muda o login da
  // conta, então só age quando a pessoa clica no botão — nunca automaticamente
  // ao abrir a página (evita que um scanner de segurança pré-visitando o link
  // consuma o token antes da confirmação real).
  async function handleConfirmar() {
    if (!token) return;
    setStatus("confirmando-envio");
    try {
      await Api.confirmarTrocaEmail(token);
      setStatus("sucesso");
    } catch (err) {
      setStatus("erro");
      setError(err.message || "Não foi possível confirmar a troca de e-mail.");
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "confirmando" && (
          <>
            <h1>Confirmar troca de e-mail</h1>
            <p>Clique no botão abaixo para confirmar o novo e-mail da sua conta.</p>
            <button className="full-width" onClick={handleConfirmar}>
              Confirmar troca de e-mail
            </button>
            <p className="hint"><Link to="/dashboard">Cancelar e voltar</Link></p>
          </>
        )}

        {status === "confirmando-envio" && <h1>Confirmando...</h1>}

        {status === "sucesso" && (
          <>
            <h1>E-mail atualizado!</h1>
            <p>Seu e-mail foi trocado com sucesso. Use o novo e-mail no seu próximo login.</p>
            <Link to="/login" className="full-width">Ir para o login</Link>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>Não foi possível confirmar</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              O link pode ter expirado. <Link to="/settings">Volte em Configurações</Link> e peça a troca novamente.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
