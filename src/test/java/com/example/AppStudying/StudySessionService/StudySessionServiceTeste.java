package com.example.AppStudying.StudySessionService;


import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.TopicRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.GroupService;
import com.example.AppStudying.services.StudySessionService;
import com.example.AppStudying.services.TimeLineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudySessionServiceTeste {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private MatterRepository matterRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeLineService timeLineService;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private StudySessionService studySessionService;


    @Test
    void inicarSessaoCorretamente(){
       Long userId = 1L;
       Long matterId = 2L;
       Long topicId = 3L;


       User user = new User();
       user.setId(userId);

       Matter matter = new Matter();
       matter.setId(matterId);

       Topic topic = new Topic();
       topic.setId(topicId);

       StudySession studySession = new StudySession();
        studySession.setId(10L);
        studySession.setUser(user);
        studySession.setMatter(matter);
        studySession.setTopic(topic);
        studySession.setInicio(LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(studySession);

        StudySession resultado = studySessionService.iniciarSessao(userId, matterId, topicId);

        assertNotNull(resultado);
        assertEquals(user, resultado.getUser());
        assertEquals(matter, resultado.getMatter());
        assertEquals(topic, resultado.getTopic());
        assertNotNull(resultado.getInicio());

        verify(userRepository, times(1)).findById(userId);
        verify(matterRepository, times(1)).findById(matterId);
        verify(topicRepository, times(1)).findById(topicId);
        verify(studySessionRepository, times(1)).save(any(StudySession.class));
        verify(groupService, times(1)).notificarSessaoAlterada(userId);
    }

    @Test
    void deveIniciarSessaoSemTopico(){
        Long userId = 1L;
        Long matterId = 2L;

        User user = new User();
        user.setId(userId);

        Matter matter = new Matter();
        matter.setId(matterId);

        StudySession studySession = new StudySession();
        studySession.setId(10L);
        studySession.setUser(user);
        studySession.setMatter(matter);
        studySession.setInicio(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(studySession);

        StudySession resultado = studySessionService.iniciarSessao(userId, matterId, null);

        assertNotNull(resultado);
        assertNull(resultado.getTopic());

        verify(topicRepository, never()).findById(any());
        verify(studySessionRepository, times(1)).save(any(StudySession.class));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado(){
        Long userId = 1L;
        Long matterId = 2L;
        Long topicId = 3L;


        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.iniciarSessao(userId, matterId, topicId);
        });

        verify(userRepository, times(1)).findById(userId);
        verify(matterRepository, never()).findById(any());
        verify(studySessionRepository, never()).save(any(StudySession.class));

    }

    @Test
    void deveLancarExcecaoQuandoMatterNaoencontrada(){
        Long userId = 1L;
        Long matterId = 2L;
        Long topicId = 3L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.findById(matterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.iniciarSessao(userId, matterId, topicId);});

            verify(userRepository, times(1)).findById(userId);
            verify(matterRepository, times(1)).findById(matterId);
            verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    void deveLancarExcecaoQuandoTopicNaoEncontrado(){
        Long userId = 1L;
        Long matterId = 2L;
        Long topicId = 3L;

        Matter matter = new Matter();
        matter.setId(matterId);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.iniciarSessao(userId, matterId, topicId);
        });

        verify(userRepository, times(1)).findById(userId);
        verify(matterRepository, times(1)).findById(matterId);
        verify(topicRepository, times(1)).findById(topicId);
        verify(studySessionRepository, never()).save(any(StudySession.class));



    }

    @Test
    void deveEncerrarSessaoComSucesso(){
        Long sessionId = 10L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Matter matter = new Matter();
        matter.setNome("Cálculo 1");

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setMatter(matter);
        studySession.setInicio(LocalDateTime.now().minusMinutes(30));

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(studySession);

        StudySession resultado = studySessionService.encerrarSessao(sessionId, userId);

        assertNotNull(resultado);
        assertNotNull(resultado.getFim());

        verify(studySessionRepository, times(1)).findById(sessionId);
        verify(studySessionRepository, times(1)).save(studySession);
        verify(timeLineService, times(1)).criarEvento(anyString(), eq(userId));
        verify(groupService, times(1)).notificarSessaoAlterada(userId);
    }

    @Test
    void deveLancarExcecaoAoEncerrarSessaoDeOutraPessoa(){
        Long sessionId = 10L;
        Long userId = 1L;
        Long outroUsuarioId = 99L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setInicio(LocalDateTime.now().minusMinutes(30));

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.encerrarSessao(sessionId, outroUsuarioId);
        });

        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    void deveLancarExcecaoAoEncerrarSessaoInexistente(){
        Long sessionId = 10L;

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.encerrarSessao(sessionId, 1L);
        });

        verify(studySessionRepository, times(1)).findById(sessionId);
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    void deveLancarExcecaoAoEncerrarSessaoJaEncerrada(){
        Long sessionId = 10L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setInicio(LocalDateTime.now().minusMinutes(30));
        studySession.setFim(LocalDateTime.now());

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.encerrarSessao(sessionId, userId);
        });

        verify(studySessionRepository, times(1)).findById(sessionId);
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    void deveCalcularDuracaoComSucesso(){
        Long sessionId = 10L;
        Long userId = 1L;
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(45);
        LocalDateTime fim = LocalDateTime.now();

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setInicio(inicio);
        studySession.setFim(fim);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        Long duracao = studySessionService.calcularDuracao(sessionId, userId);

        assertEquals(Duration.between(inicio, fim).toMinutes(), duracao);
        verify(studySessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void deveLancarExcecaoAoCalcularDuracaoDeSessaoDeOutraPessoa(){
        Long sessionId = 10L;
        Long userId = 1L;
        Long outroUsuarioId = 99L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setInicio(LocalDateTime.now().minusMinutes(45));
        studySession.setFim(LocalDateTime.now());

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.calcularDuracao(sessionId, outroUsuarioId);
        });
    }

    @Test
    void deveLancarExcecaoAoCalcularDuracaoDeSessaoInexistente(){
        Long sessionId = 10L;

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.calcularDuracao(sessionId, 1L);
        });

        verify(studySessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void deveLancarExcecaoAoCalcularDuracaoDeSessaoNaoEncerrada(){
        Long sessionId = 10L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);
        studySession.setInicio(LocalDateTime.now());

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.calcularDuracao(sessionId, userId);
        });

        verify(studySessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void deveListarSessoesPorUsuario(){
        Long userId = 1L;

        StudySession sessao1 = new StudySession();
        sessao1.setId(1L);

        StudySession sessao2 = new StudySession();
        sessao2.setId(2L);

        when(studySessionRepository.findByUserId(userId)).thenReturn(List.of(sessao1, sessao2));

        List<StudySession> resultado = studySessionService.listarPorUsuario(userId);

        assertEquals(2, resultado.size());
        verify(studySessionRepository, times(1)).findByUserId(userId);
    }

    @Test
    void deveBuscarSessaoPorIdComSucesso(){
        Long sessionId = 10L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        StudySession resultado = studySessionService.buscarPorId(sessionId, userId);

        assertEquals(sessionId, resultado.getId());
        verify(studySessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void deveLancarExcecaoAoBuscarSessaoPorIdDeOutraPessoa(){
        Long sessionId = 10L;
        Long userId = 1L;
        Long outroUsuarioId = 99L;

        User user = new User();
        user.setId(userId);

        StudySession studySession = new StudySession();
        studySession.setId(sessionId);
        studySession.setUser(user);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(studySession));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.buscarPorId(sessionId, outroUsuarioId);
        });
    }

    @Test
    void deveLancarExcecaoAoBuscarSessaoPorIdInexistente(){
        Long sessionId = 10L;

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.buscarPorId(sessionId, 1L);
        });

        verify(studySessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void deveEncontrarSessaoAtivaQuandoExiste(){
        Long userId = 1L;
        Long matterId = 2L;

        StudySession studySession = new StudySession();
        studySession.setId(10L);

        when(studySessionRepository.findByUserIdAndMatterIdAndFimIsNull(userId, matterId))
                .thenReturn(Optional.of(studySession));

        StudySession resultado = studySessionService.buscarSessaoAtiva(userId, matterId);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    void deveRetornarNuloQuandoNaoHaSessaoAtiva(){
        Long userId = 1L;
        Long matterId = 2L;

        when(studySessionRepository.findByUserIdAndMatterIdAndFimIsNull(userId, matterId))
                .thenReturn(Optional.empty());

        StudySession resultado = studySessionService.buscarSessaoAtiva(userId, matterId);

        assertNull(resultado);
    }

    @Test
    void deveAgruparResumoPorMateriaSomandoDuracao(){
        Long userId = 1L;

        Matter calculo = new Matter();
        calculo.setNome("Cálculo 1");

        LocalDateTime agora = LocalDateTime.now();

        StudySession sessao1 = new StudySession();
        sessao1.setMatter(calculo);
        sessao1.setInicio(agora.minusMinutes(30));
        sessao1.setFim(agora);

        StudySession sessao2 = new StudySession();
        sessao2.setMatter(calculo);
        sessao2.setInicio(agora.minusMinutes(20));
        sessao2.setFim(agora);

        StudySession sessaoEmAndamento = new StudySession();
        sessaoEmAndamento.setMatter(calculo);
        sessaoEmAndamento.setInicio(agora);

        when(studySessionRepository.findByUserId(userId)).thenReturn(List.of(sessao1, sessao2, sessaoEmAndamento));

        var resultado = studySessionService.resumoPorUsuario(userId);

        assertEquals(1, resultado.size());
        assertEquals("Cálculo 1", resultado.get(0).getMatterName());
        assertEquals(50, resultado.get(0).getTotalMinutes());
        assertEquals(2, resultado.get(0).getTotalSessions());
    }

}

