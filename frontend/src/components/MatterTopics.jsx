import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Api } from "../api/api";

const STATUS_ORDER = ["PENDENTE", "EM_ANDAMENTO", "CONCLUIDO"];

export function MatterTopics({ matterId }) {
  const { t } = useTranslation();
  const [topics, setTopics] = useState([]);
  const [nome, setNome] = useState("");
  const [error, setError] = useState("");

  async function load() {
    setTopics(await Api.listarTopicsPorMatter(matterId));
  }

  useEffect(() => {
    Api.listarTopicsPorMatter(matterId)
      .then(setTopics)
      .catch((err) => setError(err.message || t("matterTopics.loadError")));
  }, [matterId, t]);

  async function handleAdd(e) {
    e.preventDefault();
    if (!nome.trim()) return;
    try {
      await Api.criarTopic(matterId, nome.trim());
      setNome("");
      await load();
    } catch (err) {
      setError(err.message || t("matterTopics.addError"));
    }
  }

  async function handleCycleStatus(topic) {
    const currentIndex = STATUS_ORDER.indexOf(topic.status);
    const nextStatus = STATUS_ORDER[(currentIndex + 1) % STATUS_ORDER.length];
    try {
      await Api.atualizarTopic(topic.id, topic.nome, nextStatus);
      await load();
    } catch (err) {
      setError(err.message || t("matterTopics.updateError"));
    }
  }

  async function handleDelete(id) {
    try {
      await Api.excluirTopic(id);
      await load();
    } catch (err) {
      setError(err.message || t("matterTopics.removeError"));
    }
  }

  return (
    <div className="matter-topics">
      <form className="inline-form" onSubmit={handleAdd}>
        <input
          type="text"
          placeholder={t("matterTopics.newTopicPlaceholder")}
          value={nome}
          onChange={(e) => setNome(e.target.value)}
        />
        <button type="submit">{t("common.add")}</button>
      </form>

      {error && <div className="error-msg">{error}</div>}

      <ul className="list">
        {topics.length === 0 && <li className="empty">{t("matterTopics.noTopics")}</li>}
        {topics.map((tItem) => (
          <li key={tItem.id}>
            <div className="item-main">
              <strong>{tItem.nome}</strong>
            </div>
            <button
              type="button"
              className={`badge ${tItem.status}`}
              onClick={() => handleCycleStatus(tItem)}
              title={t("matterTopics.clickToChangeStatus")}
            >
              {t(`status.${tItem.status}`, tItem.status)}
            </button>
            <button className="danger" onClick={() => handleDelete(tItem.id)}>{t("common.remove")}</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
