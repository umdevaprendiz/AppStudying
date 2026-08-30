package com.example.AppStudying.ChatServiceTeste;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.ChatMessage;
import com.example.AppStudying.model.Conversation;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.ChatMessageRepository;
import com.example.AppStudying.repository.ConversationRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.ChatService;
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
public class ChatServiceTeste {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    void deveCriarConversaPendenteNaPrimeiraMensagem(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(conversationRepository.findEntreUsuarios(senderId, receiverId)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage resultado = chatService.enviarMensagem(senderId, receiverId, "Olá!");

        assertNotNull(resultado);
        assertEquals(RequestStatus.PENDENTE, resultado.getConversation().getStatus());
        assertEquals(sender, resultado.getConversation().getRequester());
        assertEquals(receiver, resultado.getConversation().getReceiver());
        assertFalse(resultado.isRead());

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/" + receiverId), eq(resultado));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/" + senderId), eq(resultado));
    }

    @Test
    void deveManterPendenteQuandoRequesterEnviaOutraMensagem(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(5L);
        conversation.setRequester(sender);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(conversationRepository.findEntreUsuarios(senderId, receiverId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage resultado = chatService.enviarMensagem(senderId, receiverId, "De novo!");

        assertEquals(RequestStatus.PENDENTE, resultado.getConversation().getStatus());
    }

    @Test
    void deveAutoAceitarQuandoReceiverResponde(){
        Long requesterId = 1L;
        Long receiverId = 2L;

        User requester = new User();
        requester.setId(requesterId);
        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(5L);
        conversation.setRequester(requester);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(conversationRepository.findEntreUsuarios(receiverId, requesterId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage resultado = chatService.enviarMensagem(receiverId, requesterId, "Oi, tudo bem?");

        assertEquals(RequestStatus.ACEITA, resultado.getConversation().getStatus());
    }

    @Test
    void deveEnviarMensagemNormalmenteEmConversaAceita(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(5L);
        conversation.setRequester(sender);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.ACEITA);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(conversationRepository.findEntreUsuarios(senderId, receiverId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage resultado = chatService.enviarMensagem(senderId, receiverId, "Beleza");

        assertEquals(RequestStatus.ACEITA, resultado.getConversation().getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoConversaRecusada(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(5L);
        conversation.setRequester(receiver);
        conversation.setReceiver(sender);
        conversation.setStatus(RequestStatus.RECUSADA);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(conversationRepository.findEntreUsuarios(senderId, receiverId)).thenReturn(Optional.of(conversation));

        assertThrows(IllegalStateException.class, () -> {
            chatService.enviarMensagem(senderId, receiverId, "Oi de novo");
        });

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deveLancarExcecaoQuandoRemetenteIgualDestinatario(){
        Long userId = 1L;

        assertThrows(IllegalStateException.class, () -> {
            chatService.enviarMensagem(userId, userId, "Oi");
        });

        verify(userRepository, never()).findById(any());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deveLancarExcecaoQuandoMensagemVazia(){
        Long senderId = 1L;
        Long receiverId = 2L;

        assertThrows(IllegalStateException.class, () -> {
            chatService.enviarMensagem(senderId, receiverId, "   ");
        });

        verify(userRepository, never()).findById(any());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deveLancarExcecaoQuandoRemetenteNaoEncontrado(){
        Long senderId = 1L;
        Long receiverId = 2L;

        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            chatService.enviarMensagem(senderId, receiverId, "Oi");
        });

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deveLancarExcecaoQuandoDestinatarioNaoEncontrado(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            chatService.enviarMensagem(senderId, receiverId, "Oi");
        });

        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deveListarConversaEMarcarComoLidas(){
        Long selfId = 1L;
        Long otherId = 2L;

        ChatMessage msg1 = new ChatMessage();
        msg1.setId(1L);

        when(chatMessageRepository.findConversa(selfId, otherId)).thenReturn(List.of(msg1));
        when(conversationRepository.countByReceiverIdAndStatus(eq(selfId), eq(RequestStatus.PENDENTE))).thenReturn(0L);
        when(chatMessageRepository.countByReceiverIdAndReadFalse(selfId)).thenReturn(0L);

        List<ChatMessage> resultado = chatService.listarConversa(selfId, otherId);

        assertEquals(1, resultado.size());
        verify(chatMessageRepository, times(1)).marcarComoLidas(selfId, otherId);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat-inbox/" + selfId), any(Long.class));
    }

    @Test
    void deveListarSolicitacoesPendentes(){
        Long userId = 1L;

        Conversation c1 = new Conversation();
        c1.setId(1L);

        when(conversationRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDENTE)).thenReturn(List.of(c1));

        List<Conversation> resultado = chatService.listarSolicitacoes(userId);

        assertEquals(1, resultado.size());
        verify(conversationRepository, times(1)).findByReceiverIdAndStatus(userId, RequestStatus.PENDENTE);
    }

    @Test
    void deveListarConversasAceitas(){
        Long userId = 1L;

        Conversation c1 = new Conversation();
        c1.setId(1L);

        when(conversationRepository.findPorUsuarioEStatus(userId, RequestStatus.ACEITA)).thenReturn(List.of(c1));

        List<Conversation> resultado = chatService.listarConversasAceitas(userId);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveAceitarConversaComSucesso(){
        Long conversationId = 5L;
        Long receiverId = 2L;

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        Conversation resultado = chatService.aceitarConversa(conversationId, receiverId);

        assertEquals(RequestStatus.ACEITA, resultado.getStatus());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat-inbox/" + receiverId), any(Long.class));
    }

    @Test
    void deveLancarExcecaoAoAceitarConversaDeOutraPessoa(){
        Long conversationId = 5L;
        Long receiverId = 2L;
        Long outroUsuarioId = 99L;

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThrows(IllegalStateException.class, () -> {
            chatService.aceitarConversa(conversationId, outroUsuarioId);
        });

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void deveLancarExcecaoAoAceitarConversaJaRespondida(){
        Long conversationId = 5L;
        Long receiverId = 2L;

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.ACEITA);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThrows(IllegalStateException.class, () -> {
            chatService.aceitarConversa(conversationId, receiverId);
        });

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void deveRecusarConversaComSucesso(){
        Long conversationId = 5L;
        Long receiverId = 2L;

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        Conversation resultado = chatService.recusarConversa(conversationId, receiverId);

        assertEquals(RequestStatus.RECUSADA, resultado.getStatus());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat-inbox/" + receiverId), any(Long.class));
    }

    @Test
    void deveLancarExcecaoAoRecusarConversaDeOutraPessoa(){
        Long conversationId = 5L;
        Long receiverId = 2L;
        Long outroUsuarioId = 99L;

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setReceiver(receiver);
        conversation.setStatus(RequestStatus.PENDENTE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThrows(IllegalStateException.class, () -> {
            chatService.recusarConversa(conversationId, outroUsuarioId);
        });

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void deveContarNaoLidasComSucesso(){
        Long userId = 1L;

        when(conversationRepository.countByReceiverIdAndStatus(userId, RequestStatus.PENDENTE)).thenReturn(2L);
        when(chatMessageRepository.countByReceiverIdAndReadFalse(userId)).thenReturn(3L);

        long resultado = chatService.contarNaoLidas(userId);

        assertEquals(5L, resultado);
    }
}
