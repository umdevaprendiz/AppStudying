package com.example.AppStudying.StudySessionService;


import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.TopicRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.StudySessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    }

}

