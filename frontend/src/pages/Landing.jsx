import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/auth-context";

const FEATURES = [
  { color: "#f4c43a", title: "Matérias", text: "Crie suas matérias e organize os tópicos de cada uma." },
  { color: "#3654ff", title: "Cronômetro", text: "Aperta start, estuda, aperta stop. Simples assim." },
  { color: "#ff6f91", title: "Parcerias", text: "Manda um pedido de parceria de estudo pra quem quiser." },
  { color: "#3654ff", title: "Chat", text: "Bate um papo com seus parceiros, direto no app." },
  { color: "#f4c43a", title: "Linha do tempo", text: "Registra o que rolou na sua jornada de estudos." },
  { color: "#3654ff", title: "Perfil", text: "Mostra pra galera o que você tá estudando." },
];

export function Landing() {
  const { user } = useAuth();
  if (user) return <Navigate to="/dashboard" replace />;

  return (
    <div className="landing">
      <header className="landing-nav">
        <div className="wrap landing-nav-inner">
          <span className="brand">AppStudying</span>
          <div className="landing-nav-actions">
            <Link to="/login" className="btn-outline">Entrar</Link>
            <Link to="/register" className="btn-blue">Criar conta</Link>
          </div>
        </div>
      </header>

      <section className="landing-hero">
        <div className="wrap">
          <p className="landing-eyebrow">Rede de estudantes</p>
          <h1>Encontre gente pra estudar <mark>junto</mark>.</h1>
          <p className="landing-sub">
            O AppStudying junta organização pessoal de estudos com uma rede de verdade:
            peça parceria de estudo, converse em tempo real e acompanhe sua evolução ao
            lado de outras pessoas.
          </p>
          <div className="landing-ctas">
            <Link to="/register" className="btn-blue">Criar conta</Link>
            <Link to="/login" className="btn-outline">Entrar</Link>
          </div>
        </div>
      </section>

      <section className="landing-board">
        <div className="wrap landing-grid">
          {FEATURES.map((f) => (
            <div className="landing-pin" key={f.title}>
              <span className="landing-dot" style={{ background: f.color }} />
              <h3>{f.title}</h3>
              <p>{f.text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="landing-band">
        <div className="wrap">
          <p className="landing-band-big">Transforme estudar sozinho em estudar em grupo.</p>
          <p className="landing-band-small">
            Sem grupo de WhatsApp lotado, sem combinar por mensagem perdida — tudo dentro do app.
          </p>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="wrap landing-footer-inner">
          <span>AppStudying — sua rede de estudos.</span>
          <span>Feito por Sérgio Souza.</span>
        </div>
      </footer>
    </div>
  );
}
