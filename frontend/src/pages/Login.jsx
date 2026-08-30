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

  if (user) return <Navigate to="/dashboard" replace />;

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      const loggedUser = await Api.login(email, senha);
      login(loggedUser);
      navigate("/dashboard");
    } catch {
      setError("Não foi possível entrar. Verifique e-mail e senha.");
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

        <p className="hint">Não tem conta? <Link to="/register">Cadastre-se</Link></p>
      </form>
    </div>
  );
}
