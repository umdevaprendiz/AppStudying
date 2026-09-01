import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function DeleteAccount() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [status, setStatus] = useState(() => (token ? "confirmando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : "Link inválido: token não informado."));

  // Diferente da verificação de e-mail (baixo risco, dispara sozinho ao abrir
  // a página), excluir conta é destrutivo e irreversível — e links de e-mail
  // costumam ser pré-visitados por scanners de segurança/proxies antes da
  // pessoa clicar. Por isso essa página só age quando a pessoa clica no
  // botão, nunca automaticamente.
  async function handleConfirmar() {
    if (!token) return;
    setStatus("excluindo");
    try {
      await Api.confirmarExclusaoConta(token);
      try {
        await Api.logout();
      } finally {
        logout();
      }
      setStatus("sucesso");
    } catch (err) {
      setStatus("erro");
      setError(err.message || "Não foi possível excluir sua conta.");
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "confirmando" && (
          <>
            <h1>Excluir sua conta</h1>
            <p>
              Essa ação é <strong>permanente e irreversível</strong>: todos os
              seus dados (matérias, tópicos, sessões de estudo, conversas)
              serão apagados.
            </p>
            <button className="danger full-width" onClick={handleConfirmar}>
              Sim, excluir minha conta permanentemente
            </button>
            <p className="hint"><Link to="/dashboard">Cancelar e voltar</Link></p>
          </>
        )}

        {status === "excluindo" && <h1>Excluindo sua conta...</h1>}

        {status === "sucesso" && (
          <>
            <h1>Conta excluída</h1>
            <p>Sua conta e todos os seus dados foram removidos.</p>
            <button className="full-width" onClick={() => navigate("/login")}>Ir para o login</button>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>Não foi possível excluir</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              O link pode ter expirado. <Link to="/settings">Volte em Configurações</Link> e peça um novo.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
