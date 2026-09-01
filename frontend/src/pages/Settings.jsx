import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Api } from "../api/api";
import { useAuth } from "../context/auth-context";

export function Settings() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [senhaMsg, setSenhaMsg] = useState("");
  const [senhaError, setSenhaError] = useState("");

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
