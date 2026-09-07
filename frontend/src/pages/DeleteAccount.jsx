import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function DeleteAccount() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { t } = useTranslation();

  const [status, setStatus] = useState(() => (token ? "confirmando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : t("auth.deleteAccount.invalidLink")));

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
      setError(err.message || t("auth.deleteAccount.genericError"));
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "confirmando" && (
          <>
            <h1>{t("auth.deleteAccount.confirmTitle")}</h1>
            <p>
              <Trans i18nKey="auth.deleteAccount.confirmMessage" components={{ 1: <strong /> }} />
            </p>
            <button className="danger full-width" onClick={handleConfirmar}>
              {t("auth.deleteAccount.confirmButton")}
            </button>
            <p className="hint"><Link to="/dashboard">{t("auth.deleteAccount.cancelLink")}</Link></p>
          </>
        )}

        {status === "excluindo" && <h1>{t("auth.deleteAccount.deletingTitle")}</h1>}

        {status === "sucesso" && (
          <>
            <h1>{t("auth.deleteAccount.successTitle")}</h1>
            <p>{t("auth.deleteAccount.successMessage")}</p>
            <button className="full-width" onClick={() => navigate("/login")}>{t("auth.deleteAccount.goToLogin")}</button>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>{t("auth.deleteAccount.errorTitle")}</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              <Trans i18nKey="auth.deleteAccount.expiredHint" components={{ 1: <Link to="/settings" /> }} />
            </p>
          </>
        )}
      </div>
    </div>
  );
}
