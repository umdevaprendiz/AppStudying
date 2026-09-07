import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Register() {
  const { user } = useAuth();
  const { t } = useTranslation();
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
      setError(err.message || t("auth.register.genericError"));
    }
  }

  if (cadastrado) {
    return (
      <div className="auth-wrapper">
        <div className="card">
          <h1>{t("auth.register.doneTitle")}</h1>
          <p><Trans i18nKey="auth.register.doneMessage" values={{ email: form.email }} components={{ 1: <strong /> }} /></p>
          <p className="hint">{t("auth.register.doneHint")}</p>
          <Link to="/login" className="full-width">{t("auth.register.goToLogin")}</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-wrapper">
      <form className="card" onSubmit={handleSubmit}>
        <h1>{t("auth.register.title")}</h1>

        <label htmlFor="name">{t("auth.register.name")}</label>
        <input id="name" type="text" value={form.name} onChange={updateField("name")} required />

        <label htmlFor="email">{t("auth.register.email")}</label>
        <input id="email" type="email" value={form.email} onChange={updateField("email")} required />

        <label htmlFor="password">{t("auth.register.password")}</label>
        <input id="password" type="password" value={form.password} onChange={updateField("password")} required />

        <button type="submit" className="full-width">{t("auth.register.submit")}</button>
        <div className="error-msg">{error}</div>

        <p className="hint">{t("auth.register.haveAccount")} <Link to="/login">{t("auth.register.login")}</Link></p>
      </form>
    </div>
  );
}
