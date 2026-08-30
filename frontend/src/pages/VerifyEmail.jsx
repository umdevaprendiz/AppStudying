import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Api } from "../api/api";

export function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [status, setStatus] = useState(() => (token ? "verificando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : "Link inválido: token não informado."));

  useEffect(() => {
    if (!token) return;
    Api.verificarEmail(token)
      .then(() => setStatus("sucesso"))
      .catch((err) => {
        setStatus("erro");
        setError(err.message || "Não foi possível verificar seu e-mail.");
      });
  }, [token]);

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "verificando" && <h1>Verificando seu e-mail...</h1>}

        {status === "sucesso" && (
          <>
            <h1>E-mail confirmado!</h1>
            <p>Sua conta foi ativada. Você já pode entrar.</p>
            <Link to="/login" className="full-width">Ir para o login</Link>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>Não foi possível verificar</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              O link pode ter expirado. <Link to="/login">Volte ao login</Link> e peça um novo e-mail de verificação.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
