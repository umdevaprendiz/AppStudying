import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";
import { useTheme } from "../hooks/useTheme";
import { LanguageSwitcher } from "../components/LanguageSwitcher";

export function Settings() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();
  const { t } = useTranslation();

  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [senhaMsg, setSenhaMsg] = useState("");
  const [senhaError, setSenhaError] = useState("");

  const [novoEmail, setNovoEmail] = useState("");
  const [emailSolicitado, setEmailSolicitado] = useState("");
  const [emailError, setEmailError] = useState("");

  const [exclusaoSolicitada, setExclusaoSolicitada] = useState(false);
  const [exclusaoError, setExclusaoError] = useState("");

  async function handleTrocarSenha(e) {
    e.preventDefault();
    setSenhaMsg("");
    setSenhaError("");
    try {
      await Api.alterarSenha(user.id, senhaAtual, novaSenha);
      setSenhaAtual("");
      setNovaSenha("");
      setSenhaMsg(t("settings.passwordChanged"));
    } catch (err) {
      setSenhaError(err.message || t("settings.passwordChangeError"));
    }
  }

  async function handleTrocarEmail(e) {
    e.preventDefault();
    setEmailError("");
    try {
      await Api.solicitarTrocaEmail(novoEmail);
      setEmailSolicitado(novoEmail);
      setNovoEmail("");
    } catch (err) {
      setEmailError(err.message || t("settings.emailChangeError"));
    }
  }

  async function handleSolicitarExclusao() {
    setExclusaoError("");
    const confirmou = window.confirm(t("settings.deleteConfirm"));
    if (!confirmou) return;

    try {
      await Api.solicitarExclusaoConta();
      setExclusaoSolicitada(true);
    } catch (err) {
      setExclusaoError(err.message || t("settings.deleteError"));
    }
  }

  return (
    <>
      <header className="topbar">
        <div className="brand">{t("common.appName")}</div>
        <button className="secondary" onClick={() => navigate(-1)}>{t("profile.back")}</button>
      </header>

      <main className="profile-main">
        <section className="panel wide">
          <h2>{t("settings.appearance")}</h2>
          <p className="hint" style={{ textAlign: "left", marginTop: 0 }}>
            {t("settings.appearanceHint")}
          </p>
          <button type="button" className="secondary" onClick={toggleTheme}>
            {theme === "dark" ? t("settings.useLightTheme") : t("settings.useDarkTheme")}
          </button>
        </section>

        <section className="panel wide">
          <h2>{t("settings.language")}</h2>
          <p className="hint" style={{ textAlign: "left", marginTop: 0 }}>
            {t("settings.languageHint")}
          </p>
          <LanguageSwitcher />
        </section>

        <section className="panel wide">
          <h2>{t("settings.changePassword")}</h2>
          <form className="inline-form" onSubmit={handleTrocarSenha}>
            <input
              type="password"
              placeholder={t("settings.currentPassword")}
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder={t("settings.newPassword")}
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
              required
            />
            <button type="submit">{t("common.save")}</button>
          </form>
          {senhaMsg && <p className="hint">{senhaMsg}</p>}
          {senhaError && <div className="error-msg">{senhaError}</div>}
        </section>

        <section className="panel wide">
          <h2>{t("settings.changeEmail")}</h2>
          <p className="hint" style={{ textAlign: "left", marginTop: 0 }}>
            {t("settings.currentEmail")}: <strong>{user.email}</strong>
          </p>
          {emailSolicitado ? (
            <p className="hint">
              <Trans i18nKey="settings.emailChangeRequested" values={{ email: emailSolicitado }} components={{ 1: <strong /> }} />
            </p>
          ) : (
            <form className="inline-form" onSubmit={handleTrocarEmail}>
              <input
                type="email"
                placeholder={t("settings.newEmailPlaceholder")}
                value={novoEmail}
                onChange={(e) => setNovoEmail(e.target.value)}
                required
              />
              <button type="submit">{t("settings.requestChange")}</button>
            </form>
          )}
          {emailError && <div className="error-msg">{emailError}</div>}
        </section>

        <section className="panel wide">
          <h2>{t("settings.dangerZone")}</h2>
          {exclusaoSolicitada ? (
            <p className="hint">
              <Trans i18nKey="settings.deleteRequested" values={{ email: user.email }} components={{ 1: <strong /> }} />
            </p>
          ) : (
            <>
              <p className="hint">{t("settings.deleteWarning")}</p>
              <button className="danger" onClick={handleSolicitarExclusao}>{t("settings.deleteButton")}</button>
              {exclusaoError && <div className="error-msg">{exclusaoError}</div>}
            </>
          )}
        </section>
      </main>
    </>
  );
}
