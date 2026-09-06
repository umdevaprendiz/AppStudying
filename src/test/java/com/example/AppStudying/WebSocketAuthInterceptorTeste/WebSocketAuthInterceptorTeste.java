package com.example.AppStudying.WebSocketAuthInterceptorTeste;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.GroupMember;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.GroupMemberRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.webSocketConfig.WebSocketAuthInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebSocketAuthInterceptorTeste {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    private static final Principal PRINCIPAL = () -> "user@email.com";

    private Message<byte[]> subscribeMessage(String destination, Principal principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        if (principal != null) {
            accessor.setUser(principal);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private User usuario(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@email.com");
        return user;
    }

    @Test
    void devePermitirInscricaoNoProprioTopicoDeUsuario() {
        Long userId = 1L;
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(userId)));

        Message<?> resultado = interceptor.preSend(subscribeMessage("/topic/chat/" + userId, PRINCIPAL), null);

        assertNotNull(resultado);
    }

    @Test
    void deveBloquearInscricaoNoTopicoDeOutroUsuario() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(1L)));

        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(subscribeMessage("/topic/chat/999", PRINCIPAL), null));
    }

    @Test
    void deveBloquearInscricaoSemAutenticacao() {
        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(subscribeMessage("/topic/chat/1", null), null));
    }

    // Bug real encontrado ao testar a presença ao vivo dos grupos: o número
    // no fim de "/topic/group-presence/{id}" é um id de GRUPO, não de
    // usuário — comparar com o id do usuário autenticado rejeitava toda
    // inscrição de grupo, mesmo de membros aceitos.
    @Test
    void devePermitirInscricaoEmPresencaDeGrupoQuandoMembroAceito() {
        Long userId = 1L, groupId = 5L;
        GroupMember membro = new GroupMember();
        membro.setStatus(RequestStatus.ACEITA);

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(userId)));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(membro));

        Message<?> resultado = interceptor.preSend(subscribeMessage("/topic/group-presence/" + groupId, PRINCIPAL), null);

        assertNotNull(resultado);
    }

    @Test
    void devePermitirInscricaoEmChatDeGrupoQuandoMembroAceito() {
        Long userId = 1L, groupId = 5L;
        GroupMember membro = new GroupMember();
        membro.setStatus(RequestStatus.ACEITA);

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(userId)));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(membro));

        Message<?> resultado = interceptor.preSend(subscribeMessage("/topic/group-chat/" + groupId, PRINCIPAL), null);

        assertNotNull(resultado);
    }

    @Test
    void deveBloquearInscricaoEmGrupoQuandoNaoEMembro() {
        Long userId = 1L, groupId = 5L;
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(userId)));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(subscribeMessage("/topic/group-presence/" + groupId, PRINCIPAL), null));
    }

    @Test
    void deveBloquearInscricaoEmGrupoQuandoConviteAindaPendente() {
        Long userId = 1L, groupId = 5L;
        GroupMember membro = new GroupMember();
        membro.setStatus(RequestStatus.PENDENTE);

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(usuario(userId)));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.of(membro));

        assertThrows(AccessDeniedException.class, () ->
                interceptor.preSend(subscribeMessage("/topic/group-chat/" + groupId, PRINCIPAL), null));
    }

    @Test
    void deveIgnorarComandosQueNaoSaoSubscribe() {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();

        Message<?> resultado = interceptor.preSend(message, null);

        assertNotNull(resultado);
    }
}
