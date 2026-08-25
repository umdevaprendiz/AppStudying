package com.example.AppStudying.services;

import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.TopicRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudySessionService {

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatterRepository matterRepository;

    public StudySession iniciarSessao(Long userId, Long matterId, Long topicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
        Matter matter = matterRepository.findById(matterId)
                .orElseThrow(() -> new IllegalStateException("Matéria não encontrada!"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalStateException("Tópico não encontrado!"));

        StudySession studySession = new StudySession();
        studySession.setUser(user);
        studySession.setTopic(topic);
        studySession.setMatter(matter);
        studySession.setInicio(LocalDateTime.now());

        return studySessionRepository.save(studySession);
    }

    public StudySession encerrarSessao(Long sessionId) {
        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Sessão de estudos não encontrado!"));

        if (studySession.getFim() != null) {
            throw new IllegalStateException("Sua sessão já foi encerrada!");
        }
        studySession.setFim(LocalDateTime.now());

        return studySessionRepository.save(studySession);

    }

    public Long calcularDuracao(Long sessionId) {
        StudySession session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Sessão não encontrada!"));

        if (session.getFim() == null) {
            throw new IllegalStateException("Sua sessão ainda não foi encerrada!");
        }

        Duration duration = Duration.between(session.getInicio(), session.getFim());
        return duration.toMinutes();
    }

    public List<StudySession> listarPorUsuario(Long userId) {
        return studySessionRepository.findByUserId(userId);
    }

    public StudySession buscarPorId(Long id) {
        return studySessionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Sessão de estudos não encontrada!"));
    }
}
