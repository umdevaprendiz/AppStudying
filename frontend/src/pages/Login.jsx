import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");
  const [errorStatus, setErrorStatus] = useState(null);
  const [reenvioStatus, setReenvioStatus] = useState("");

  if (user) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setErrorStatus(null);
    setReenvioStatus("");
    try {
      const loggedUser = await Api.login(email, senha);
      login(loggedUser);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || t("auth.login.genericError"));
      setErrorStatus(err.status ?? null);
    }
  }

  async function handleReenviar() {
    setReenvioStatus("");
    try {
      await Api.reenviarVerificacao(email);
      setReenvioStatus(t("auth.login.resendSent"));
    } catch (err) {
      setReenvioStatus(err.message || t("auth.login.resendError"));
    }
  }

  return (
    <div className="auth-wrapper">
      <form className="card" onSubmit={handleSubmit}>
        <h1>{t("auth.login.title")}</h1>

        <label htmlFor="email">{t("auth.login.email")}</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor="senha">{t("auth.login.password")}</label>
        <input id="senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required />

        <button type="submit" className="full-width">{t("auth.login.submit")}</button>
        <div className="error-msg">{error}</div>

        {errorStatus === 403 && (
          <div className="hint">
            <button type="button" className="secondary" onClick={handleReenviar}>
              {t("auth.login.resendButton")}
            </button>
            {reenvioStatus && <p>{reenvioStatus}</p>}
          </div>
        )}

        <p className="hint">{t("auth.login.noAccount")} <Link to="/register">{t("auth.login.signUp")}</Link></p>
      </form>
    </div>
  );
}
