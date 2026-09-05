import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";
import { useTheme } from "../hooks/useTheme";

export function Settings() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

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
      setSenhaMsg("Senha alterada com sucesso.");
    } catch (err) {
      setSenhaError(err.message || "Não foi possível alterar a senha.");
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
      setEmailError(err.message || "Não foi possível solicitar a troca de e-mail.");
    }
  }

  async function handleSolicitarExclusao() {
    setExclusaoError("");
    const confirmou = window.confirm(
      "Vamos enviar um e-mail de confirmação. Sua conta só é excluída depois que você clicar no link. Continuar?"
    );
    if (!confirmou) return;

    try {
      await Api.solicitarExclusaoConta();
      setExclusaoSolicitada(true);
    } catch (err) {
      setExclusaoError(err.message || "Não foi possível solicitar a exclusão.");
    }
  }

  return (
    <>
      <header className="topbar">
        <div className="brand">AppStudying</div>
        <button className="secondary" onClick={() => navigate(-1)}>Voltar</button>
      </header>

      <main className="profile-main">
        <section className="panel wide">
          <h2>Aparência</h2>
          <p className="hint" style={{ textAlign: "left", marginTop: 0 }}>
            Escolha como o AppStudying aparece pra você neste dispositivo.
          </p>
          <button type="button" className="secondary" onClick={toggleTheme}>
            {theme === "dark" ? "Usar tema claro" : "Usar tema escuro"}
          </button>
        </section>

        <section className="panel wide">
          <h2>Trocar senha</h2>
          <form className="inline-form" onSubmit={handleTrocarSenha}>
            <input
              type="password"
              placeholder="Senha atual"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Nova senha"
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
              required
            />
            <button type="submit">Salvar</button>
          </form>
          {senhaMsg && <p className="hint">{senhaMsg}</p>}
          {senhaError && <div className="error-msg">{senhaError}</div>}
        </section>

        <section className="panel wide">
          <h2>Trocar e-mail</h2>
          <p className="hint" style={{ textAlign: "left", marginTop: 0 }}>
            E-mail atual: <strong>{user.email}</strong>
          </p>
          {emailSolicitado ? (
            <p className="hint">
              Enviamos um e-mail de confirmação para <strong>{emailSolicitado}</strong>.
              Seu e-mail só muda depois que você clicar no link recebido lá.
            </p>
          ) : (
            <form className="inline-form" onSubmit={handleTrocarEmail}>
              <input
                type="email"
                placeholder="Novo e-mail"
                value={novoEmail}
                onChange={(e) => setNovoEmail(e.target.value)}
                required
              />
              <button type="submit">Solicitar troca</button>
            </form>
          )}
          {emailError && <div className="error-msg">{emailError}</div>}
        </section>

        <section className="panel wide">
          <h2>Zona de perigo</h2>
          {exclusaoSolicitada ? (
            <p className="hint">
              Enviamos um e-mail de confirmação para <strong>{user.email}</strong>.
              Sua conta só será excluída depois que você clicar no link recebido.
            </p>
          ) : (
            <>
              <p className="hint">
                Excluir sua conta apaga permanentemente todos os seus dados
                (matérias, tópicos, sessões de estudo, conversas). Essa ação
                não pode ser desfeita.
              </p>
              <button className="danger" onClick={handleSolicitarExclusao}>Excluir minha conta</button>
              {exclusaoError && <div className="error-msg">{exclusaoError}</div>}
            </>
          )}
        </section>
      </main>
    </>
  );
}
