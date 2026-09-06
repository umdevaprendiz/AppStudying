package com.example.AppStudying.AccountDeletionServiceTeste;

import com.example.AppStudying.repository.ChatMessageRepository;
import com.example.AppStudying.repository.ConversationRepository;
import com.example.AppStudying.repository.GroupMemberRepository;
import com.example.AppStudying.repository.GroupMessageRepository;
import com.example.AppStudying.repository.GroupRepository;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.StudyRequestRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.TimeLineRepository;
import com.example.AppStudying.repository.TopicRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.AccountDeletionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccountDeletionServiceTeste {

    @InjectMocks
    private AccountDeletionService accountDeletionService;

    @Mock
    private StudySessionRepository studySessionRepository;
    @Mock
    private StudyRequestRepository studyRequestRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private MatterRepository matterRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private TimeLineRepository timeLineRepository;
    @Mock
    private GroupMessageRepository groupMessageRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void deveExcluirTodosOsDadosDoUsuarioNaOrdemCorreta(){
        Long userId = 1L;

        accountDeletionService.excluirConta(userId);

        // A ordem importa: nenhuma FK do banco tem ON DELETE CASCADE, então
        // filhos precisam sumir antes dos pais (sessão/pedido antes de
        // tópico/matéria, mensagem antes de conversa, grupos que ela é dona
        // antes do próprio grupo, tudo antes do usuário).
        InOrder ordem = inOrder(studySessionRepository, studyRequestRepository, topicRepository,
                matterRepository, chatMessageRepository, conversationRepository, timeLineRepository,
                groupMessageRepository, groupMemberRepository, groupRepository, userRepository);

        ordem.verify(studySessionRepository).deleteByUserId(userId);
        ordem.verify(studyRequestRepository).deleteEnvolvendoUsuario(userId);
        ordem.verify(topicRepository).deleteByMatterUserId(userId);
        ordem.verify(matterRepository).deleteByUserId(userId);
        ordem.verify(chatMessageRepository).deleteEnvolvendoUsuario(userId);
        ordem.verify(conversationRepository).deleteEnvolvendoUsuario(userId);
        ordem.verify(timeLineRepository).deleteByUserId(userId);
        ordem.verify(groupMessageRepository).deleteByGroupOwnerId(userId);
        ordem.verify(groupMemberRepository).deleteByGroupOwnerId(userId);
        ordem.verify(groupRepository).deleteByOwnerId(userId);
        ordem.verify(groupMessageRepository).deleteBySenderId(userId);
        ordem.verify(groupMemberRepository).deleteByUserId(userId);
        ordem.verify(userRepository).deleteById(userId);

        verify(userRepository).deleteById(userId);
    }
}
