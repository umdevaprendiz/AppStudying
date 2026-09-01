package com.example.AppStudying.TopicServiceTeste;

import com.example.AppStudying.enums.TypeStatus;
import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.TopicRepository;
import com.example.AppStudying.services.TopicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopicServiceTeste {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MatterRepository matterRepository;

    @InjectMocks
    private TopicService topicService;

    private Matter matterDoUsuario(Long matterId, Long userId) {
        User user = new User();
        user.setId(userId);
        Matter matter = new Matter();
        matter.setId(matterId);
        matter.setUser(user);
        return matter;
    }

    @Test
    void deveCriarTopicComSucesso() {
        Long userId = 1L;
        Long matterId = 10L;
        Matter matter = matterDoUsuario(matterId, userId);

        Topic topic = new Topic();
        topic.setNome("Derivadas");

        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(topicRepository.save(topic)).thenReturn(topic);

        Topic resultado = topicService.criarTopic(topic, matterId, userId);

        assertEquals(matter, resultado.getMatter());
        assertEquals(TypeStatus.PENDENTE, resultado.getStatus());
        verify(topicRepository, times(1)).save(topic);
    }

    @Test
    void deveManterStatusInformadoAoCriarTopic() {
        Long userId = 1L;
        Long matterId = 10L;
        Matter matter = matterDoUsuario(matterId, userId);

        Topic topic = new Topic();
        topic.setNome("Integrais");
        topic.setStatus(TypeStatus.CONCLUIDO);

        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(topicRepository.save(topic)).thenReturn(topic);

        Topic resultado = topicService.criarTopic(topic, matterId, userId);

        assertEquals(TypeStatus.CONCLUIDO, resultado.getStatus());
    }

    @Test
    void deveLancarExcecaoAoCriarTopicEmMateriaInexistente() {
        Long matterId = 10L;
        Topic topic = new Topic();

        when(matterRepository.findById(matterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            topicService.criarTopic(topic, matterId, 1L);
        });

        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void deveLancarExcecaoAoCriarTopicEmMateriaDeOutroUsuario() {
        Long matterId = 10L;
        Matter matter = matterDoUsuario(matterId, 2L);
        Topic topic = new Topic();

        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));

        assertThrows(IllegalStateException.class, () -> {
            topicService.criarTopic(topic, matterId, 1L);
        });

        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void deveBuscarTopicPorIdComSucesso() {
        Long userId = 1L;
        Matter matter = matterDoUsuario(10L, userId);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));

        Topic resultado = topicService.buscarPorId(5L, userId);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void deveLancarExcecaoAoBuscarTopicInexistente() {
        when(topicRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            topicService.buscarPorId(5L, 1L);
        });
    }

    @Test
    void deveLancarExcecaoAoBuscarTopicDeOutroUsuario() {
        Matter matter = matterDoUsuario(10L, 2L);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> {
            topicService.buscarPorId(5L, 1L);
        });
    }

    @Test
    void deveListarTopicsPorMateria() {
        Long userId = 1L;
        Long matterId = 10L;
        Matter matter = matterDoUsuario(matterId, userId);

        Topic topic1 = new Topic();
        Topic topic2 = new Topic();

        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));
        when(topicRepository.findByMatterId(matterId)).thenReturn(List.of(topic1, topic2));

        List<Topic> resultado = topicService.listarPorMatter(matterId, userId);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveLancarExcecaoAoListarTopicsDeMateriaDeOutroUsuario() {
        Long matterId = 10L;
        Matter matter = matterDoUsuario(matterId, 2L);

        when(matterRepository.findById(matterId)).thenReturn(Optional.of(matter));

        assertThrows(IllegalStateException.class, () -> {
            topicService.listarPorMatter(matterId, 1L);
        });

        verify(topicRepository, never()).findByMatterId(any());
    }

    @Test
    void deveAtualizarTopicComSucesso() {
        Long userId = 1L;
        Matter matter = matterDoUsuario(10L, userId);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);
        topic.setNome("Nome antigo");
        topic.setStatus(TypeStatus.PENDENTE);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));
        when(topicRepository.save(topic)).thenReturn(topic);

        Topic resultado = topicService.atualizarTopic(5L, "Nome novo", TypeStatus.EM_ANDAMENTO, userId);

        assertEquals("Nome novo", resultado.getNome());
        assertEquals(TypeStatus.EM_ANDAMENTO, resultado.getStatus());
    }

    @Test
    void deveLancarExcecaoAoAtualizarTopicDeOutroUsuario() {
        Matter matter = matterDoUsuario(10L, 2L);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> {
            topicService.atualizarTopic(5L, "Novo nome", TypeStatus.CONCLUIDO, 1L);
        });

        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void deveExcluirTopicComSucesso() {
        Long userId = 1L;
        Matter matter = matterDoUsuario(10L, userId);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));

        topicService.excluirTopic(5L, userId);

        verify(topicRepository, times(1)).delete(topic);
    }

    @Test
    void deveLancarExcecaoAoExcluirTopicDeOutroUsuario() {
        Matter matter = matterDoUsuario(10L, 2L);
        Topic topic = new Topic();
        topic.setId(5L);
        topic.setMatter(matter);

        when(topicRepository.findById(5L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> {
            topicService.excluirTopic(5L, 1L);
        });

        verify(topicRepository, never()).delete(any(Topic.class));
    }
}
