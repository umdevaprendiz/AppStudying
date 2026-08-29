package com.example.AppStudying.StudyRequestServiceTeste;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudyRequest;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudyRequestRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.StudyRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudyRequestServiceTeste {

    @Mock
    private StudyRequestRepository studyRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatterRepository matterRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private StudyRequestService studyRequestService;

    @Test
    void deveEnviarSolicitacaoComSucesso(){
        Long requesterId = 1L;
        Long receiverId = 2L;

        User requester = new User();
        requester.setId(requesterId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(studyRequestRepository.save(any(StudyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyRequest resultado = studyRequestService.enviarSolicitacao(requesterId, receiverId, null, "Vamos estudar?");

        assertNotNull(resultado);
        assertEquals(requester, resultado.getRequester());
        assertEquals(receiver, resultado.getReceiver());
        assertEquals(RequestStatus.PENDENTE, resultado.getStatus());
        assertNotNull(resultado.getCreatedAt());
        assertNull(resultado.getMatter());

        verify(studyRequestRepository, times(1)).save(any(StudyRequest.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/study-requests/" + receiverId), eq(resultado));
    }

    @Test
    void deveEnviarSolicitacaoComMateriaComSucesso(){
        Long requesterId = 1L;
        Long receiverId = 2L;
        Long matterId = 3L;

        User requester = new User();
        requester.setId(requesterId);

        User receiver = new User();
        receiver.setId(receiverId);

        Matter matter = new Matter();
        matter.setId(matterId);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(studyRequestRepository.save(any(StudyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyRequest resultado = studyRequestService.enviarSolicitacao(requesterId, receiverId, matterId, null);

        assertEquals(matter, resultado.getMatter());
        verify(matterRepository, times(1)).findById(matterId);
    }

    @Test
    void deveLancarExcecaoQuandoRequesterIgualReceiver(){
        Long userId = 1L;

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.enviarSolicitacao(userId, userId, null, null);
        });

        verify(userRepository, never()).findById(any());
        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveLancarExcecaoQuandoRequesterNaoEncontrado(){
        Long requesterId = 1L;
        Long receiverId = 2L;

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.enviarSolicitacao(requesterId, receiverId, null, null);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveLancarExcecaoQuandoReceiverNaoEncontrado(){
        Long requesterId = 1L;
        Long receiverId = 2L;

        User requester = new User();
        requester.setId(requesterId);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.enviarSolicitacao(requesterId, receiverId, null, null);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveLancarExcecaoQuandoMateriaNaoEncontrada(){
        Long requesterId = 1L;
        Long receiverId = 2L;
        Long matterId = 3L;

        User requester = new User();
        requester.setId(requesterId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(matterRepository.findById(matterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.enviarSolicitacao(requesterId, receiverId, matterId, null);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveAceitarSolicitacaoComSucesso(){
        Long requestId = 10L;
        Long requesterId = 1L;

        User requester = new User();
        requester.setId(requesterId);

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setId(requestId);
        studyRequest.setRequester(requester);
        studyRequest.setStatus(RequestStatus.PENDENTE);

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.of(studyRequest));
        when(studyRequestRepository.save(any(StudyRequest.class))).thenReturn(studyRequest);

        StudyRequest resultado = studyRequestService.aceitarSolicitacao(requestId);

        assertEquals(RequestStatus.ACEITA, resultado.getStatus());
        assertNotNull(resultado.getRespondedAt());

        verify(studyRequestRepository, times(1)).save(studyRequest);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/study-requests/" + requesterId), eq(studyRequest));
    }

    @Test
    void deveLancarExcecaoAoAceitarSolicitacaoInexistente(){
        Long requestId = 10L;

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.aceitarSolicitacao(requestId);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveLancarExcecaoAoAceitarSolicitacaoJaRespondida(){
        Long requestId = 10L;

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setId(requestId);
        studyRequest.setStatus(RequestStatus.ACEITA);

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.of(studyRequest));

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.aceitarSolicitacao(requestId);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveRecusarSolicitacaoComSucesso(){
        Long requestId = 10L;
        Long requesterId = 1L;

        User requester = new User();
        requester.setId(requesterId);

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setId(requestId);
        studyRequest.setRequester(requester);
        studyRequest.setStatus(RequestStatus.PENDENTE);

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.of(studyRequest));
        when(studyRequestRepository.save(any(StudyRequest.class))).thenReturn(studyRequest);

        StudyRequest resultado = studyRequestService.recusarSolicitacao(requestId);

        assertEquals(RequestStatus.RECUSADA, resultado.getStatus());
        assertNotNull(resultado.getRespondedAt());

        verify(studyRequestRepository, times(1)).save(studyRequest);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/study-requests/" + requesterId), eq(studyRequest));
    }

    @Test
    void deveLancarExcecaoAoRecusarSolicitacaoJaRespondida(){
        Long requestId = 10L;

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setId(requestId);
        studyRequest.setStatus(RequestStatus.RECUSADA);

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.of(studyRequest));

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.recusarSolicitacao(requestId);
        });

        verify(studyRequestRepository, never()).save(any(StudyRequest.class));
    }

    @Test
    void deveListarSolicitacoesRecebidas(){
        Long userId = 1L;

        StudyRequest request1 = new StudyRequest();
        request1.setId(1L);

        StudyRequest request2 = new StudyRequest();
        request2.setId(2L);

        when(studyRequestRepository.findByReceiverId(userId)).thenReturn(List.of(request1, request2));

        List<StudyRequest> resultado = studyRequestService.listarRecebidas(userId);

        assertEquals(2, resultado.size());
        verify(studyRequestRepository, times(1)).findByReceiverId(userId);
    }

    @Test
    void deveListarSolicitacoesEnviadas(){
        Long userId = 1L;

        StudyRequest request1 = new StudyRequest();
        request1.setId(1L);

        when(studyRequestRepository.findByRequesterId(userId)).thenReturn(List.of(request1));

        List<StudyRequest> resultado = studyRequestService.listarEnviadas(userId);

        assertEquals(1, resultado.size());
        verify(studyRequestRepository, times(1)).findByRequesterId(userId);
    }

    @Test
    void deveBuscarSolicitacaoPorIdComSucesso(){
        Long requestId = 10L;

        StudyRequest studyRequest = new StudyRequest();
        studyRequest.setId(requestId);

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.of(studyRequest));

        StudyRequest resultado = studyRequestService.buscarPorId(requestId);

        assertEquals(requestId, resultado.getId());
        verify(studyRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void deveLancarExcecaoAoBuscarSolicitacaoPorIdInexistente(){
        Long requestId = 10L;

        when(studyRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studyRequestService.buscarPorId(requestId);
        });

        verify(studyRequestRepository, times(1)).findById(requestId);
    }

}
