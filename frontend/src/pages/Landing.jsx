import { Link, Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/auth-context";
import { LanguageSwitcher } from "../components/LanguageSwitcher";

const FEATURE_KEYS = [
  { key: "matters", color: "#f4c43a" },
  { key: "timer", color: "#3654ff" },
  { key: "partnerships", color: "#ff6f91" },
  { key: "chat", color: "#3654ff" },
  { key: "timeline", color: "#f4c43a" },
  { key: "profile", color: "#3654ff" },
];

export function Landing() {
  const { user } = useAuth();
  const { t } = useTranslation();
  if (user) return <Navigate to="/dashboard" replace />;

  return (
    <div className="landing">
      <header className="landing-nav">
        <div className="wrap landing-nav-inner">
          <span className="brand">{t("common.appName")}</span>
          <div className="landing-nav-actions">
            <LanguageSwitcher />
            <Link to="/login" className="btn-outline">{t("landing.login")}</Link>
            <Link to="/register" className="btn-blue">{t("landing.createAccount")}</Link>
          </div>
        </div>
      </header>

      <section className="landing-hero">
        <div className="wrap">
          <p className="landing-eyebrow">{t("landing.eyebrow")}</p>
          <h1>
            {t("landing.heroTitlePre")} <mark>{t("landing.heroTitleMark")}</mark>.
          </h1>
          <p className="landing-sub">{t("landing.heroSub")}</p>
          <div className="landing-ctas">
            <Link to="/register" className="btn-blue">{t("landing.createAccount")}</Link>
            <Link to="/login" className="btn-outline">{t("landing.login")}</Link>
          </div>
        </div>
      </section>

      <section className="landing-board">
        <div className="wrap landing-grid">
          {FEATURE_KEYS.map((f) => (
            <div className="landing-pin" key={f.key}>
              <span className="landing-dot" style={{ background: f.color }} />
              <h3>{t(`landing.features.${f.key}.title`)}</h3>
              <p>{t(`landing.features.${f.key}.text`)}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="landing-band">
        <div className="wrap">
          <p className="landing-band-big">{t("landing.bandBig")}</p>
          <p className="landing-band-small">{t("landing.bandSmall")}</p>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="wrap landing-footer-inner">
          <span>{t("landing.footerTagline")}</span>
          <span>{t("landing.footerCredit")}</span>
        </div>
      </footer>
    </div>
  );
}
