import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import { Api } from "../api/api";

export function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const { t } = useTranslation();
  const [status, setStatus] = useState(() => (token ? "verificando" : "erro"));
  const [error, setError] = useState(() => (token ? "" : t("auth.verifyEmail.invalidLink")));
  // O React.StrictMode (só em dev) roda o efeito duas vezes de propósito; sem
  // essa guarda, a segunda chamada usaria um token já consumido pela primeira
  // e mostraria "inválido" por cima de uma verificação que já deu certo.
  const chamouRef = useRef(false);

  useEffect(() => {
    if (!token || chamouRef.current) return;
    chamouRef.current = true;
    Api.verificarEmail(token)
      .then(() => setStatus("sucesso"))
      .catch((err) => {
        setStatus("erro");
        setError(err.message || t("auth.verifyEmail.genericError"));
      });
  }, [token, t]);

  return (
    <div className="auth-wrapper">
      <div className="card">
        {status === "verificando" && <h1>{t("auth.verifyEmail.verifying")}</h1>}

        {status === "sucesso" && (
          <>
            <h1>{t("auth.verifyEmail.successTitle")}</h1>
            <p>{t("auth.verifyEmail.successMessage")}</p>
            <Link to="/login" className="full-width">{t("auth.verifyEmail.goToLogin")}</Link>
          </>
        )}

        {status === "erro" && (
          <>
            <h1>{t("auth.verifyEmail.errorTitle")}</h1>
            <p className="error-msg">{error}</p>
            <p className="hint">
              <Trans i18nKey="auth.verifyEmail.expiredHint" components={{ 1: <Link to="/login" /> }} />
            </p>
          </>
        )}
      </div>
    </div>
  );
}
