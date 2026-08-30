import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");
  const [reenvioStatus, setReenvioStatus] = useState("");

  if (user) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setReenvioStatus("");
    try {
      const loggedUser = await Api.login(email, senha);
      login(loggedUser);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "Não foi possível entrar. Verifique e-mail e senha.");
    }
  }

  async function handleReenviar() {
    setReenvioStatus("");
    try {
      await Api.reenviarVerificacao(email);
      setReenvioStatus("E-mail de verificação reenviado. Confira sua caixa de entrada.");
    } catch (err) {
      setReenvioStatus(err.message || "Não foi possível reenviar o e-mail.");
    }
  }

  return (
    <div className="auth-wrapper">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Entrar no AppStudying</h1>

        <label htmlFor="email">E-mail</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label htmlFor="senha">Senha</label>
        <input id="senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required />

        <button type="submit" className="full-width">Entrar</button>
        <div className="error-msg">{error}</div>

        {error.includes("Confirme seu e-mail") && (
          <div className="hint">
            <button type="button" className="secondary" onClick={handleReenviar}>
              Reenviar e-mail de verificação
            </button>
            {reenvioStatus && <p>{reenvioStatus}</p>}
          </div>
        )}

        <p className="hint">Não tem conta? <Link to="/register">Cadastre-se</Link></p>
      </form>
    </div>
  );
}
