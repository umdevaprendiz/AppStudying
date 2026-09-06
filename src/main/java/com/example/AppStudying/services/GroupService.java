package com.example.AppStudying.services;

import com.example.AppStudying.dto.GroupMemberPresenceDTO;
import com.example.AppStudying.dto.GroupSummaryDTO;
import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.Group;
import com.example.AppStudying.model.GroupMember;
import com.example.AppStudying.model.GroupMessage;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.GroupMemberRepository;
import com.example.AppStudying.repository.GroupMessageRepository;
import com.example.AppStudying.repository.GroupRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    // Mesmo limite descrito na proposta: uma sala de estudo em grupo cabe até
    // 30 pessoas ACEITAS (convites pendentes/recusados não contam pra conta).
    private static final int MAX_MEMBROS = 30;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Group criarGrupo(Long ownerId, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("O grupo precisa de um nome!");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));

        Group group = new Group();
        group.setName(name.trim());
        group.setOwner(owner);
        group.setCreatedAt(LocalDateTime.now());
        group = groupRepository.save(group);

        GroupMember dono = new GroupMember();
        dono.setGroup(group);
        dono.setUser(owner);
        dono.setInvitedBy(owner);
        dono.setStatus(RequestStatus.ACEITA);
        dono.setCreatedAt(LocalDateTime.now());
        dono.setRespondedAt(LocalDateTime.now());
        groupMemberRepository.save(dono);

        return group;
    }

    public GroupMember convidar(Long groupId, Long inviterId, Long inviteeId) {
        if (inviterId.equals(inviteeId)) {
            throw new IllegalStateException("Você não pode convidar a si mesmo!");
        }

        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, inviterId);

        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new IllegalStateException("Usuário convidado não encontrado!"));

        long aceitos = groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA);
        if (aceitos >= MAX_MEMBROS) {
            throw new IllegalStateException("Esse grupo já atingiu o limite de 30 membros!");
        }

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));

        GroupMember membro = groupMemberRepository.findByGroupIdAndUserId(groupId, inviteeId).orElse(null);

        if (membro != null) {
            if (membro.getStatus() == RequestStatus.ACEITA) {
                throw new IllegalStateException("Essa pessoa já faz parte do grupo!");
            }
            if (membro.getStatus() == RequestStatus.PENDENTE) {
                throw new IllegalStateException("Essa pessoa já tem um convite pendente para esse grupo!");
            }
            // RECUSADA: reenviar reaproveita a mesma linha em vez de duplicar
            // (ver UK_group_member_group_user na migration).
            membro.setStatus(RequestStatus.PENDENTE);
            membro.setInvitedBy(inviter);
            membro.setCreatedAt(LocalDateTime.now());
            membro.setRespondedAt(null);
        } else {
            membro = new GroupMember();
            membro.setGroup(group);
            membro.setUser(invitee);
            membro.setInvitedBy(inviter);
            membro.setStatus(RequestStatus.PENDENTE);
            membro.setCreatedAt(LocalDateTime.now());
        }

        GroupMember salvo = groupMemberRepository.save(membro);
        messagingTemplate.convertAndSend("/topic/group-invites/" + inviteeId, salvo);

        return salvo;
    }

    public GroupMember aceitarConvite(Long memberId, Long currentUserId) {
        GroupMember membro = buscarMembroPorId(memberId);

        if (!membro.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para responder esse convite!");
        }
        if (membro.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Esse convite já foi respondido!");
        }

        long aceitos = groupMemberRepository.countByGroupIdAndStatus(membro.getGroup().getId(), RequestStatus.ACEITA);
        if (aceitos >= MAX_MEMBROS) {
            throw new IllegalStateException("Esse grupo já atingiu o limite de 30 membros!");
        }

        membro.setStatus(RequestStatus.ACEITA);
        membro.setRespondedAt(LocalDateTime.now());
        GroupMember salvo = groupMemberRepository.save(membro);

        broadcastPresenca(salvo.getGroup().getId());
        return salvo;
    }

    public GroupMember recusarConvite(Long memberId, Long currentUserId) {
        GroupMember membro = buscarMembroPorId(memberId);

        if (!membro.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para responder esse convite!");
        }
        if (membro.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Esse convite já foi respondido!");
        }

        membro.setStatus(RequestStatus.RECUSADA);
        membro.setRespondedAt(LocalDateTime.now());
        return groupMemberRepository.save(membro);
    }

    public void sair(Long groupId, Long userId) {
        Group group = buscarGrupoPorId(groupId);

        if (group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("O dono não pode sair do grupo. Exclua o grupo se quiser encerrá-lo.");
        }

        GroupMember membro = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("Você não faz parte desse grupo!"));

        groupMemberRepository.delete(membro);
        broadcastPresenca(groupId);
    }

    public void removerMembro(Long groupId, Long targetUserId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);

        if (!group.getOwner().getId().equals(requesterId)) {
            throw new IllegalStateException("Você não tem permissão para remover membros desse grupo!");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Você não pode remover a si mesmo. Exclua o grupo se quiser encerrá-lo.");
        }

        GroupMember membro = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalStateException("Essa pessoa não faz parte desse grupo!"));

        groupMemberRepository.delete(membro);
        broadcastPresenca(groupId);
    }

    public void excluirGrupo(Long groupId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);

        if (!group.getOwner().getId().equals(requesterId)) {
            throw new IllegalStateException("Só o dono pode excluir esse grupo!");
        }

        groupMessageRepository.deleteByGroupId(groupId);
        groupMemberRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);
    }

    public List<GroupSummaryDTO> listarMeusGrupos(Long userId) {
        return groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.ACEITA).stream()
                .map(membro -> montarResumo(membro.getGroup()))
                .toList();
    }

    public List<GroupMember> listarConvites(Long userId) {
        return groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.PENDENTE);
    }

    public List<GroupMember> listarConvitesPendentesDoGrupo(Long groupId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, requesterId);
        return groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.PENDENTE);
    }

    public GroupSummaryDTO buscarResumo(Long groupId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, requesterId);
        return montarResumo(group);
    }

    public List<GroupMemberPresenceDTO> listarPresenca(Long groupId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, requesterId);
        return montarPresenca(groupId);
    }

    public GroupMessage enviarMensagem(Long groupId, Long senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("A mensagem não pode estar vazia!");
        }

        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));

        GroupMessage mensagem = new GroupMessage();
        mensagem.setGroup(group);
        mensagem.setSender(sender);
        mensagem.setContent(content);
        mensagem.setSentAt(LocalDateTime.now());

        GroupMessage salva = groupMessageRepository.save(mensagem);
        messagingTemplate.convertAndSend("/topic/group-chat/" + groupId, salva);

        return salva;
    }

    public List<GroupMessage> listarMensagens(Long groupId, Long requesterId) {
        Group group = buscarGrupoPorId(groupId);
        verificarMembroAceito(group, requesterId);
        return groupMessageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }

    // Chamado pelo StudySessionService sempre que alguém inicia/encerra uma
    // sessão de estudo, pra empurrar a atualização de presença em tempo real
    // pra todo grupo aceito em que essa pessoa está.
    public void notificarSessaoAlterada(Long userId) {
        for (GroupMember membro : groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.ACEITA)) {
            broadcastPresenca(membro.getGroup().getId());
        }
    }

    public Group buscarGrupoPorId(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Grupo não encontrado!"));
    }

    private GroupMember buscarMembroPorId(Long id) {
        return groupMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Convite não encontrado!"));
    }

    private void verificarMembroAceito(Group group, Long userId) {
        groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId)
                .filter(m -> m.getStatus() == RequestStatus.ACEITA)
                .orElseThrow(() -> new IllegalStateException("Você não tem permissão para acessar esse grupo!"));
    }

    private GroupSummaryDTO montarResumo(Group group) {
        long memberCount = groupMemberRepository.countByGroupIdAndStatus(group.getId(), RequestStatus.ACEITA);
        long studyingCount = montarPresenca(group.getId()).stream().filter(GroupMemberPresenceDTO::isStudying).count();

        return new GroupSummaryDTO(
                group.getId(),
                group.getName(),
                group.getOwner().getId(),
                group.getOwner().getName(),
                group.getCreatedAt(),
                memberCount,
                studyingCount
        );
    }

    private List<GroupMemberPresenceDTO> montarPresenca(Long groupId) {
        return groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.ACEITA).stream()
                .map(membro -> {
                    User user = membro.getUser();
                    Optional<StudySession> ativa = studySessionRepository
                            .findFirstByUserIdAndFimIsNullOrderByInicioDesc(user.getId());

                    if (ativa.isEmpty()) {
                        return new GroupMemberPresenceDTO(user.getId(), user.getName(), false, null, null, null);
                    }

                    StudySession sessao = ativa.get();
                    String matterName = sessao.getMatter() != null ? sessao.getMatter().getNome() : null;
                    String topicName = sessao.getTopic() != null ? sessao.getTopic().getNome() : null;
                    return new GroupMemberPresenceDTO(user.getId(), user.getName(), true, matterName, topicName, sessao.getInicio());
                })
                .toList();
    }

    private void broadcastPresenca(Long groupId) {
        messagingTemplate.convertAndSend("/topic/group-presence/" + groupId, montarPresenca(groupId));
    }
}
