package com.example.AppStudying.services;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Nenhuma FK do banco tem ON DELETE CASCADE (todas RESTRICT por omissao), e
// nao ha cascade nas entidades JPA — apagar um usuario direto quebraria com
// violacao de FK. Esse service centraliza a ordem certa de exclusao (filhos
// antes dos pais), usada tanto pela exclusao pedida pelo usuario quanto pelo
// expurgo automatico de contas inativas, pra nao ter duas implementacoes
// divergentes da mesma logica destrutiva.
@Service
public class AccountDeletionService {

    @Autowired
    private StudySessionRepository studySessionRepository;
    @Autowired
    private StudyRequestRepository studyRequestRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private MatterRepository matterRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private TimeLineRepository timeLineRepository;
    @Autowired
    private GroupMessageRepository groupMessageRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void excluirConta(Long userId) {
        studySessionRepository.deleteByUserId(userId);
        studyRequestRepository.deleteEnvolvendoUsuario(userId);
        topicRepository.deleteByMatterUserId(userId);
        matterRepository.deleteByUserId(userId);
        chatMessageRepository.deleteEnvolvendoUsuario(userId);
        conversationRepository.deleteEnvolvendoUsuario(userId);
        timeLineRepository.deleteByUserId(userId);
        // Grupos que essa pessoa é dona: mensagens e membros somem antes do
        // grupo em si (mesma regra de filhos-antes-dos-pais). Depois, a
        // participação dela em grupos de outras pessoas.
        groupMessageRepository.deleteByGroupOwnerId(userId);
        groupMemberRepository.deleteByGroupOwnerId(userId);
        groupRepository.deleteByOwnerId(userId);
        groupMessageRepository.deleteBySenderId(userId);
        groupMemberRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
