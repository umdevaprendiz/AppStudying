package com.example.AppStudying.services;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudyRequest;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudyRequestRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyRequestService {

    @Autowired
    private StudyRequestRepository studyRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatterRepository matterRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public StudyRequest enviarSolicitacao(Long requesterId, Long receiverId, Long matterId, String message) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalStateException("Você não pode enviar uma solicitação para si mesmo!");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalStateException("Usuário solicitante não encontrado!"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalStateException("Usuário destinatário não encontrado!"));

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setRequester(requester);
        studyRequest.setReceiver(receiver);
        studyRequest.setMessage(message);
        studyRequest.setStatus(RequestStatus.PENDENTE);
        studyRequest.setCreatedAt(LocalDateTime.now());

        if (matterId != null) {
            Matter matter = matterRepository.findById(matterId)
                    .orElseThrow(() -> new IllegalStateException("Matéria não encontrada!"));
            studyRequest.setMatter(matter);
        }

        StudyRequest salva = studyRequestRepository.save(studyRequest);
        notificar(receiverId, salva);

        return salva;
    }

    public StudyRequest aceitarSolicitacao(Long requestId) {
        StudyRequest studyRequest = buscarPorId(requestId);

        if (studyRequest.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Essa solicitação já foi respondida!");
        }

        studyRequest.setStatus(RequestStatus.ACEITA);
        studyRequest.setRespondedAt(LocalDateTime.now());

        StudyRequest salva = studyRequestRepository.save(studyRequest);
        notificar(salva.getRequester().getId(), salva);

        return salva;
    }

    public StudyRequest recusarSolicitacao(Long requestId) {
        StudyRequest studyRequest = buscarPorId(requestId);

        if (studyRequest.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Essa solicitação já foi respondida!");
        }

        studyRequest.setStatus(RequestStatus.RECUSADA);
        studyRequest.setRespondedAt(LocalDateTime.now());

        StudyRequest salva = studyRequestRepository.save(studyRequest);
        notificar(salva.getRequester().getId(), salva);

        return salva;
    }

    public List<StudyRequest> listarRecebidas(Long userId) {
        return studyRequestRepository.findByReceiverId(userId);
    }

    public List<StudyRequest> listarEnviadas(Long userId) {
        return studyRequestRepository.findByRequesterId(userId);
    }

    public StudyRequest buscarPorId(Long id) {
        return studyRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Solicitação não encontrada!"));
    }

    private void notificar(Long userId, StudyRequest studyRequest) {
        messagingTemplate.convertAndSend("/topic/study-requests/" + userId, studyRequest);
    }
}
