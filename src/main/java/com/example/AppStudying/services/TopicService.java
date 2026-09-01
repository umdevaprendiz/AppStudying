package com.example.AppStudying.services;

import com.example.AppStudying.enums.TypeStatus;
import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private MatterRepository matterRepository;

    public Topic criarTopic(Topic topic, Long matterId, Long currentUserId) {
        topic.setId(null);

        Matter matter = matterRepository.findById(matterId)
                .orElseThrow(() -> new IllegalStateException("Matéria não encontrada"));

        if (!matter.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para adicionar tópicos a essa matéria!");
        }

        topic.setMatter(matter);
        if (topic.getStatus() == null) {
            topic.setStatus(TypeStatus.PENDENTE);
        }

        return topicRepository.save(topic);
    }

    public Topic buscarPorId(Long id, Long currentUserId) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Tópico não encontrado"));
        verificarDono(topic, currentUserId);
        return topic;
    }

    public List<Topic> listarPorMatter(Long matterId, Long currentUserId) {
        Matter matter = matterRepository.findById(matterId)
                .orElseThrow(() -> new IllegalStateException("Matéria não encontrada"));

        if (!matter.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para ver os tópicos dessa matéria!");
        }

        return topicRepository.findByMatterId(matterId);
    }

    public Topic atualizarTopic(Long id, String novoNome, TypeStatus novoStatus, Long currentUserId) {
        Topic topic = buscarPorId(id, currentUserId);
        topic.setNome(novoNome);
        topic.setStatus(novoStatus);
        return topicRepository.save(topic);
    }

    public void excluirTopic(Long id, Long currentUserId) {
        Topic topic = buscarPorId(id, currentUserId);
        topicRepository.delete(topic);
    }

    private void verificarDono(Topic topic, Long currentUserId) {
        if (!topic.getMatter().getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para acessar esse tópico!");
        }
    }
}
