import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Register() {
  const { user } = useAuth();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [cadastrado, setCadastrado] = useState(false);

  if (user) return <Navigate to="/dashboard" replace />;

  function updateField(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      await Api.registrarUser(form);
      setCadastrado(true);
    } catch (err) {
      setError(err.message || "Não foi possível concluir o cadastro.");
    }
  }

  if (cadastrado) {
    return (
      <div className="auth-wrapper">
        <div className="card">
          <h1>Quase lá!</h1>
          <p>Enviamos um e-mail de confirmação para <strong>{form.email}</strong>.</p>
          <p className="hint">Clique no link recebido pra ativar sua conta e poder entrar.</p>
          <Link to="/login" className="full-width">Ir para o login</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-wrapper">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Criar conta</h1>

        <label htmlFor="name">Nome</label>
        <input id="name" type="text" value={form.name} onChange={updateField("name")} required />

        <label htmlFor="email">E-mail</label>
        <input id="email" type="email" value={form.email} onChange={updateField("email")} required />

        <label htmlFor="password">Senha</label>
        <input id="password" type="password" value={form.password} onChange={updateField("password")} required />

        <button type="submit" className="full-width">Cadastrar</button>
        <div className="error-msg">{error}</div>

        <p className="hint">Já tem conta? <Link to="/login">Entrar</Link></p>
      </form>
    </div>
  );
}
