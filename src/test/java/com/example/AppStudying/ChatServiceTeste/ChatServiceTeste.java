package com.example.AppStudying.ChatServiceTeste;

import com.example.AppStudying.model.ChatMessage;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.ChatMessageRepository;
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
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    void deveEnviarMensagemComSucesso(){
        Long senderId = 1L;
        Long receiverId = 2L;

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage resultado = chatService.enviarMensagem(senderId, receiverId, "Olá!");

        assertNotNull(resultado);
        assertEquals(sender, resultado.getSender());
        assertEquals(receiver, resultado.getReceiver());
        assertEquals("Olá!", resultado.getContent());
        assertNotNull(resultado.getSentAt());

        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/" + receiverId), eq(resultado));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/" + senderId), eq(resultado));
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
    void deveListarConversaComSucesso(){
        Long userId1 = 1L;
        Long userId2 = 2L;

        ChatMessage msg1 = new ChatMessage();
        msg1.setId(1L);

        ChatMessage msg2 = new ChatMessage();
        msg2.setId(2L);

        when(chatMessageRepository.findConversa(userId1, userId2)).thenReturn(List.of(msg1, msg2));

        List<ChatMessage> resultado = chatService.listarConversa(userId1, userId2);

        assertEquals(2, resultado.size());
        verify(chatMessageRepository, times(1)).findConversa(userId1, userId2);
    }
}
