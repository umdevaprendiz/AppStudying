import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import { Api } from "../api/api";

export function ConfirmEmailChange() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const { t } = useTranslation();

  const [status, setStatus] = useState(() => (token ? "confirmando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : t("auth.confirmEmailChange.invalidLink")));

  // Mesmo padrão da exclusão de conta: troca de e-mail muda o login da
  // conta, então só age quando a pessoa clica no botão — nunca automaticamente
  // ao abrir a página (evita que um scanner de segurança pré-visitando o link
  // consuma o token antes da confirmação real).
  async function handleConfirmar() {
    if (!token) return;
    setStatus("confirmando-envio");
    try {
      await Api.confirmarTrocaEmail(token);
      setStatus("sucesso");
    } catch (err) {
      setStatus("erro");
      setError(err.message || t("auth.confirmEmailChange.genericError"));
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "confirmando" && (
          <>
            <h1>{t("auth.confirmEmailChange.title")}</h1>
            <p>{t("auth.confirmEmailChange.message")}</p>
            <button className="full-width" onClick={handleConfirmar}>
              {t("auth.confirmEmailChange.confirmButton")}
            </button>
            <p className="hint"><Link to="/dashboard">{t("auth.confirmEmailChange.cancelLink")}</Link></p>
          </>
        )}

        {status === "confirmando-envio" && <h1>{t("auth.confirmEmailChange.confirming")}</h1>}

        {status === "sucesso" && (
          <>
            <h1>{t("auth.confirmEmailChange.successTitle")}</h1>
            <p>{t("auth.confirmEmailChange.successMessage")}</p>
            <Link to="/login" className="full-width">{t("auth.confirmEmailChange.goToLogin")}</Link>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>{t("auth.confirmEmailChange.errorTitle")}</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              <Trans i18nKey="auth.confirmEmailChange.expiredHint" components={{ 1: <Link to="/settings" /> }} />
            </p>
          </>
        )}
      </div>
    </div>
  );
}
