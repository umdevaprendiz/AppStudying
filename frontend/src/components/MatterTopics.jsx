import { useEffect, useState } from "react";
import { Api } from "../api/api";

const STATUS_LABEL = {
  PENDENTE: "Pendente",
  EM_ANDAMENTO: "Em andamento",
  CONCLUIDO: "Concluído",
};

const STATUS_ORDER = ["PENDENTE", "EM_ANDAMENTO", "CONCLUIDO"];

export function MatterTopics({ matterId }) {
  const [topics, setTopics] = useState([]);
  const [nome, setNome] = useState("");
  const [error, setError] = useState("");

  async function load() {
    setTopics(await Api.listarTopicsPorMatter(matterId));
  }

  useEffect(() => {
    Api.listarTopicsPorMatter(matterId)
      .then(setTopics)
      .catch((err) => setError(err.message || "Não foi possível carregar os tópicos."));
  }, [matterId]);

  async function handleAdd(e) {
    e.preventDefault();
    if (!nome.trim()) return;
    try {
      await Api.criarTopic(matterId, nome.trim());
      setNome("");
      await load();
    } catch (err) {
      setError(err.message || "Não foi possível adicionar o tópico.");
    }
  }

  async function handleCycleStatus(topic) {
    const currentIndex = STATUS_ORDER.indexOf(topic.status);
    const nextStatus = STATUS_ORDER[(currentIndex + 1) % STATUS_ORDER.length];
    try {
      await Api.atualizarTopic(topic.id, topic.nome, nextStatus);
      await load();
    } catch (err) {
      setError(err.message || "Não foi possível atualizar o tópico.");
    }
  }

  async function handleDelete(id) {
    try {
      await Api.excluirTopic(id);
      await load();
    } catch (err) {
      setError(err.message || "Não foi possível remover o tópico.");
    }
  }

  return (
    <div className="matter-topics">
      <form className="inline-form" onSubmit={handleAdd}>
        <input
          type="text"
          placeholder="Novo tópico"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
        />
        <button type="submit">Adicionar</button>
      </form>

      {error && <div className="error-msg">{error}</div>}

      <ul className="list">
        {topics.length === 0 && <li className="empty">Nenhum tópico ainda.</li>}
        {topics.map((t) => (
          <li key={t.id}>
            <div className="item-main">
              <strong>{t.nome}</strong>
            </div>
            <button
              type="button"
              className={`badge ${t.status}`}
              onClick={() => handleCycleStatus(t)}
              title="Clique para mudar o status"
            >
              {STATUS_LABEL[t.status] ?? t.status}
            </button>
            <button className="danger" onClick={() => handleDelete(t.id)}>Remover</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
