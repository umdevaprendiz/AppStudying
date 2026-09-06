package com.example.AppStudying.GroupServiceTeste;

import com.example.AppStudying.dto.GroupMemberPresenceDTO;
import com.example.AppStudying.dto.GroupSummaryDTO;
import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.Group;
import com.example.AppStudying.model.GroupMember;
import com.example.AppStudying.model.GroupMessage;
import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.GroupMemberRepository;
import com.example.AppStudying.repository.GroupMessageRepository;
import com.example.AppStudying.repository.GroupRepository;
import com.example.AppStudying.repository.StudySessionRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTeste {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMessageRepository groupMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GroupService groupService;

    private User usuario(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário " + id);
        return user;
    }

    private Group grupo(Long id, User owner) {
        Group group = new Group();
        group.setId(id);
        group.setName("Cálculo Squad");
        group.setOwner(owner);
        group.setCreatedAt(LocalDateTime.now());
        return group;
    }

    private GroupMember membro(Group group, User user, RequestStatus status) {
        GroupMember membro = new GroupMember();
        membro.setId(user.getId() * 100);
        membro.setGroup(group);
        membro.setUser(user);
        membro.setStatus(status);
        return membro;
    }

    // ---------- criarGrupo ----------

    @Test
    void deveCriarGrupoComSucessoEAdicionarDonoComoMembroAceito() {
        Long ownerId = 1L;
        User owner = usuario(ownerId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        Group resultado = groupService.criarGrupo(ownerId, "Cálculo Squad");

        assertEquals("Cálculo Squad", resultado.getName());
        assertEquals(owner, resultado.getOwner());
        assertNotNull(resultado.getCreatedAt());

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(captor.capture());
        assertEquals(owner, captor.getValue().getUser());
        assertEquals(RequestStatus.ACEITA, captor.getValue().getStatus());
    }

    @Test
    void deveLancarExcecaoAoCriarGrupoComNomeVazio() {
        assertThrows(IllegalStateException.class, () -> groupService.criarGrupo(1L, "  "));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCriarGrupoComDonoInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> groupService.criarGrupo(1L, "Grupo"));
        verify(groupRepository, never()).save(any());
    }

    // ---------- convidar ----------

    @Test
    void deveConvidarComSucesso() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User inviter = usuario(inviterId);
        User invitee = usuario(inviteeId);
        Group group = grupo(groupId, inviter);
        GroupMember membroInviter = membro(group, inviter, RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.of(membroInviter));
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(1L);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviteeId)).thenReturn(Optional.empty());
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupMember resultado = groupService.convidar(groupId, inviterId, inviteeId);

        assertEquals(RequestStatus.PENDENTE, resultado.getStatus());
        assertEquals(invitee, resultado.getUser());
        assertEquals(inviter, resultado.getInvitedBy());
        verify(messagingTemplate).convertAndSend(eq("/topic/group-invites/" + inviteeId), eq(resultado));
    }

    @Test
    void deveReenviarConviteReaproveitandoLinhaRecusadaAnteriormente() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User inviter = usuario(inviterId);
        User invitee = usuario(inviteeId);
        Group group = grupo(groupId, inviter);
        GroupMember membroInviter = membro(group, inviter, RequestStatus.ACEITA);
        GroupMember convitedoRecusado = membro(group, invitee, RequestStatus.RECUSADA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.of(membroInviter));
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(1L);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviteeId)).thenReturn(Optional.of(convitedoRecusado));
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupMember resultado = groupService.convidar(groupId, inviterId, inviteeId);

        assertEquals(RequestStatus.PENDENTE, resultado.getStatus());
        assertNull(resultado.getRespondedAt());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConvidarASiMesmo() {
        assertThrows(IllegalStateException.class, () -> groupService.convidar(1L, 1L, 1L));
        verify(groupRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoConvidarQuandoNaoEMembroAceito() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User owner = usuario(99L);
        Group group = grupo(groupId, owner);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> groupService.convidar(groupId, inviterId, inviteeId));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConvidarQuandoGrupoAtingiuLimite() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User inviter = usuario(inviterId);
        User invitee = usuario(inviteeId);
        Group group = grupo(groupId, inviter);
        GroupMember membroInviter = membro(group, inviter, RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.of(membroInviter));
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(30L);

        assertThrows(IllegalStateException.class, () -> groupService.convidar(groupId, inviterId, inviteeId));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConvidarQuemJaEMembro() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User inviter = usuario(inviterId);
        User invitee = usuario(inviteeId);
        Group group = grupo(groupId, inviter);
        GroupMember membroInviter = membro(group, inviter, RequestStatus.ACEITA);
        GroupMember jaMembro = membro(group, invitee, RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.of(membroInviter));
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(1L);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviteeId)).thenReturn(Optional.of(jaMembro));

        assertThrows(IllegalStateException.class, () -> groupService.convidar(groupId, inviterId, inviteeId));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoConvidarQuemJaTemConvitePendente() {
        Long groupId = 1L, inviterId = 1L, inviteeId = 2L;
        User inviter = usuario(inviterId);
        User invitee = usuario(inviteeId);
        Group group = grupo(groupId, inviter);
        GroupMember membroInviter = membro(group, inviter, RequestStatus.ACEITA);
        GroupMember pendente = membro(group, invitee, RequestStatus.PENDENTE);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviterId)).thenReturn(Optional.of(membroInviter));
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(1L);
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, inviteeId)).thenReturn(Optional.of(pendente));

        assertThrows(IllegalStateException.class, () -> groupService.convidar(groupId, inviterId, inviteeId));
        verify(groupMemberRepository, never()).save(any());
    }

    // ---------- aceitarConvite / recusarConvite ----------

    @Test
    void deveAceitarConviteComSucesso() {
        Long memberId = 10L, userId = 2L, groupId = 1L;
        User user = usuario(userId);
        Group group = grupo(groupId, usuario(1L));
        GroupMember convite = membro(group, user, RequestStatus.PENDENTE);
        convite.setId(memberId);

        when(groupMemberRepository.findById(memberId)).thenReturn(Optional.of(convite));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(5L);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(convite);
        when(groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(List.of());

        GroupMember resultado = groupService.aceitarConvite(memberId, userId);

        assertEquals(RequestStatus.ACEITA, resultado.getStatus());
        assertNotNull(resultado.getRespondedAt());
        verify(messagingTemplate).convertAndSend(eq("/topic/group-presence/" + groupId), any(List.class));
    }

    @Test
    void deveLancarExcecaoAoAceitarConviteDeOutraPessoa() {
        Long memberId = 10L;
        GroupMember convite = membro(grupo(1L, usuario(1L)), usuario(2L), RequestStatus.PENDENTE);

        when(groupMemberRepository.findById(memberId)).thenReturn(Optional.of(convite));

        assertThrows(IllegalStateException.class, () -> groupService.aceitarConvite(memberId, 99L));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAceitarConviteJaRespondido() {
        Long memberId = 10L, userId = 2L;
        GroupMember convite = membro(grupo(1L, usuario(1L)), usuario(userId), RequestStatus.ACEITA);

        when(groupMemberRepository.findById(memberId)).thenReturn(Optional.of(convite));

        assertThrows(IllegalStateException.class, () -> groupService.aceitarConvite(memberId, userId));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAceitarConviteQuandoGrupoJaEstaCheio() {
        Long memberId = 10L, userId = 2L, groupId = 1L;
        GroupMember convite = membro(grupo(groupId, usuario(1L)), usuario(userId), RequestStatus.PENDENTE);

        when(groupMemberRepository.findById(memberId)).thenReturn(Optional.of(convite));
        when(groupMemberRepository.countByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(30L);

        assertThrows(IllegalStateException.class, () -> groupService.aceitarConvite(memberId, userId));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void deveRecusarConviteComSucesso() {
        Long memberId = 10L, userId = 2L;
        GroupMember convite = membro(grupo(1L, usuario(1L)), usuario(userId), RequestStatus.PENDENTE);

        when(groupMemberRepository.findById(memberId)).thenReturn(Optional.of(convite));
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(convite);

        GroupMember resultado = groupService.recusarConvite(memberId, userId);

        assertEquals(RequestStatus.RECUSADA, resultado.getStatus());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    // ---------- sair / removerMembro / excluirGrupo ----------

    @Test
    void deveSairDoGrupoComSucesso() {
        Long groupId = 1L, userId = 2L;
        User owner = usuario(1L);
        Group group = grupo(groupId, owner);
        GroupMember membro = membro(group, usuario(userId), RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(membro));
        when(groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(List.of());

        groupService.sair(groupId, userId);

        verify(groupMemberRepository).delete(membro);
        verify(messagingTemplate).convertAndSend(eq("/topic/group-presence/" + groupId), any(List.class));
    }

    @Test
    void deveLancarExcecaoQuandoDonoTentaSairDoGrupo() {
        Long groupId = 1L, ownerId = 1L;
        Group group = grupo(groupId, usuario(ownerId));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class, () -> groupService.sair(groupId, ownerId));
        verify(groupMemberRepository, never()).delete(any());
    }

    @Test
    void deveRemoverMembroComSucessoQuandoRequisitanteEDono() {
        Long groupId = 1L, ownerId = 1L, targetId = 2L;
        Group group = grupo(groupId, usuario(ownerId));
        GroupMember membro = membro(group, usuario(targetId), RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, targetId)).thenReturn(Optional.of(membro));
        when(groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.ACEITA)).thenReturn(List.of());

        groupService.removerMembro(groupId, targetId, ownerId);

        verify(groupMemberRepository).delete(membro);
    }

    @Test
    void deveLancarExcecaoAoRemoverMembroQuandoRequisitanteNaoEDono() {
        Long groupId = 1L, ownerId = 1L, targetId = 2L, outraPessoaId = 3L;
        Group group = grupo(groupId, usuario(ownerId));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class, () -> groupService.removerMembro(groupId, targetId, outraPessoaId));
        verify(groupMemberRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoDonoTentarRemoverASiMesmo() {
        Long groupId = 1L, ownerId = 1L;
        Group group = grupo(groupId, usuario(ownerId));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class, () -> groupService.removerMembro(groupId, ownerId, ownerId));
        verify(groupMemberRepository, never()).delete(any());
    }

    @Test
    void deveExcluirGrupoComSucessoQuandoRequisitanteEDono() {
        Long groupId = 1L, ownerId = 1L;
        Group group = grupo(groupId, usuario(ownerId));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        groupService.excluirGrupo(groupId, ownerId);

        verify(groupMessageRepository).deleteByGroupId(groupId);
        verify(groupMemberRepository).deleteByGroupId(groupId);
        verify(groupRepository).delete(group);
    }

    @Test
    void deveLancarExcecaoAoExcluirGrupoQuandoNaoEDono() {
        Long groupId = 1L, ownerId = 1L, outraPessoaId = 2L;
        Group group = grupo(groupId, usuario(ownerId));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class, () -> groupService.excluirGrupo(groupId, outraPessoaId));
        verify(groupRepository, never()).delete(any());
    }

    // ---------- listagens ----------

    @Test
    void deveListarMeusGrupos() {
        Long userId = 2L;
        Group group = grupo(1L, usuario(1L));
        GroupMember membro = membro(group, usuario(userId), RequestStatus.ACEITA);

        when(groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.ACEITA)).thenReturn(List.of(membro));
        when(groupMemberRepository.countByGroupIdAndStatus(group.getId(), RequestStatus.ACEITA)).thenReturn(3L);
        when(groupMemberRepository.findByGroupIdAndStatus(group.getId(), RequestStatus.ACEITA)).thenReturn(List.of());

        List<GroupSummaryDTO> resultado = groupService.listarMeusGrupos(userId);

        assertEquals(1, resultado.size());
        assertEquals("Cálculo Squad", resultado.get(0).getName());
        assertEquals(3, resultado.get(0).getMemberCount());
    }

    @Test
    void deveListarConvitesPendentes() {
        Long userId = 2L;
        GroupMember convite = membro(grupo(1L, usuario(1L)), usuario(userId), RequestStatus.PENDENTE);

        when(groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.PENDENTE)).thenReturn(List.of(convite));

        List<GroupMember> resultado = groupService.listarConvites(userId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveListarConvitesPendentesDoGrupoQuandoRequisitanteEMembro() {
        Long groupId = 1L, requesterId = 1L;
        User owner = usuario(requesterId);
        Group group = grupo(groupId, owner);
        GroupMember requester = membro(group, owner, RequestStatus.ACEITA);
        GroupMember pendente = membro(group, usuario(2L), RequestStatus.PENDENTE);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.of(requester));
        when(groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.PENDENTE)).thenReturn(List.of(pendente));

        List<GroupMember> resultado = groupService.listarConvitesPendentesDoGrupo(groupId, requesterId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveLancarExcecaoAoListarConvitesPendentesSemSerMembro() {
        Long groupId = 1L, requesterId = 2L;
        Group group = grupo(groupId, usuario(1L));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> groupService.listarConvitesPendentesDoGrupo(groupId, requesterId));
    }

    // ---------- presença ----------

    @Test
    void deveListarPresencaComMembroEstudandoEMembroOcioso() {
        Long groupId = 1L, requesterId = 1L, estudandoId = 2L, ociosoId = 3L;
        User owner = usuario(requesterId);
        Group group = grupo(groupId, owner);
        GroupMember requester = membro(group, owner, RequestStatus.ACEITA);

        User estudando = usuario(estudandoId);
        User ocioso = usuario(ociosoId);
        GroupMember membroEstudando = membro(group, estudando, RequestStatus.ACEITA);
        GroupMember membroOcioso = membro(group, ocioso, RequestStatus.ACEITA);

        Matter matter = new Matter();
        matter.setNome("Cálculo I");
        Topic topic = new Topic();
        topic.setNome("Derivadas");
        StudySession sessaoAtiva = new StudySession();
        sessaoAtiva.setMatter(matter);
        sessaoAtiva.setTopic(topic);
        sessaoAtiva.setInicio(LocalDateTime.now().minusMinutes(10));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.of(requester));
        when(groupMemberRepository.findByGroupIdAndStatus(groupId, RequestStatus.ACEITA))
                .thenReturn(List.of(membroEstudando, membroOcioso));
        when(studySessionRepository.findFirstByUserIdAndFimIsNullOrderByInicioDesc(estudandoId))
                .thenReturn(Optional.of(sessaoAtiva));
        when(studySessionRepository.findFirstByUserIdAndFimIsNullOrderByInicioDesc(ociosoId))
                .thenReturn(Optional.empty());

        List<GroupMemberPresenceDTO> resultado = groupService.listarPresenca(groupId, requesterId);

        assertEquals(2, resultado.size());
        GroupMemberPresenceDTO dtoEstudando = resultado.stream().filter(d -> d.getUserId().equals(estudandoId)).findFirst().orElseThrow();
        assertTrue(dtoEstudando.isStudying());
        assertEquals("Cálculo I", dtoEstudando.getMatterName());
        assertEquals("Derivadas", dtoEstudando.getTopicName());

        GroupMemberPresenceDTO dtoOcioso = resultado.stream().filter(d -> d.getUserId().equals(ociosoId)).findFirst().orElseThrow();
        assertFalse(dtoOcioso.isStudying());
        assertNull(dtoOcioso.getMatterName());
    }

    @Test
    void deveLancarExcecaoAoListarPresencaSemSerMembroAceito() {
        Long groupId = 1L, requesterId = 2L;
        Group group = grupo(groupId, usuario(1L));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> groupService.listarPresenca(groupId, requesterId));
    }

    // ---------- notificarSessaoAlterada ----------

    @Test
    void deveNotificarTodosOsGruposAceitosQuandoSessaoMuda() {
        Long userId = 5L;
        Group grupo1 = grupo(1L, usuario(1L));
        Group grupo2 = grupo(2L, usuario(1L));

        when(groupMemberRepository.findByUserIdAndStatus(userId, RequestStatus.ACEITA))
                .thenReturn(List.of(membro(grupo1, usuario(userId), RequestStatus.ACEITA), membro(grupo2, usuario(userId), RequestStatus.ACEITA)));
        when(groupMemberRepository.findByGroupIdAndStatus(anyLong(), eq(RequestStatus.ACEITA))).thenReturn(List.of());

        groupService.notificarSessaoAlterada(userId);

        verify(messagingTemplate).convertAndSend(eq("/topic/group-presence/1"), any(List.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/group-presence/2"), any(List.class));
    }

    // ---------- chat ----------

    @Test
    void deveEnviarMensagemComSucesso() {
        Long groupId = 1L, senderId = 2L;
        User sender = usuario(senderId);
        Group group = grupo(groupId, usuario(1L));
        GroupMember membro = membro(group, sender, RequestStatus.ACEITA);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, senderId)).thenReturn(Optional.of(membro));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(groupMessageRepository.save(any(GroupMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupMessage resultado = groupService.enviarMensagem(groupId, senderId, "Bora estudar!");

        assertEquals("Bora estudar!", resultado.getContent());
        assertEquals(sender, resultado.getSender());
        verify(messagingTemplate).convertAndSend(eq("/topic/group-chat/" + groupId), eq(resultado));
    }

    @Test
    void deveLancarExcecaoAoEnviarMensagemVazia() {
        assertThrows(IllegalStateException.class, () -> groupService.enviarMensagem(1L, 1L, "   "));
        verify(groupRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoEnviarMensagemSemSerMembro() {
        Long groupId = 1L, senderId = 2L;
        Group group = grupo(groupId, usuario(1L));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, senderId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> groupService.enviarMensagem(groupId, senderId, "Oi"));
        verify(groupMessageRepository, never()).save(any());
    }

    @Test
    void deveListarMensagensComSucesso() {
        Long groupId = 1L, requesterId = 1L;
        User owner = usuario(requesterId);
        Group group = grupo(groupId, owner);
        GroupMember membro = membro(group, owner, RequestStatus.ACEITA);

        GroupMessage msg = new GroupMessage();
        msg.setId(1L);
        msg.setContent("Oi pessoal");

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)).thenReturn(Optional.of(membro));
        when(groupMessageRepository.findByGroupIdOrderBySentAtAsc(groupId)).thenReturn(List.of(msg));

        List<GroupMessage> resultado = groupService.listarMensagens(groupId, requesterId);

        assertEquals(1, resultado.size());
        assertEquals("Oi pessoal", resultado.get(0).getContent());
    }
}
