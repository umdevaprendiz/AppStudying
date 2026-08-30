import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Register() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", cpf: "", email: "", password: "" });
  const [error, setError] = useState("");

  if (user) return <Navigate to="/dashboard" replace />;

  function updateField(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      const createdUser = await Api.registrarUser(form);
      login(createdUser);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "Não foi possível concluir o cadastro.");
    }
  }

  return (
    <div className="auth-wrapper">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Criar conta</h1>

        <label htmlFor="name">Nome</label>
        <input id="name" type="text" value={form.name} onChange={updateField("name")} required />

        <label htmlFor="cpf">CPF</label>
        <input id="cpf" type="text" value={form.cpf} onChange={updateField("cpf")} required />

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
