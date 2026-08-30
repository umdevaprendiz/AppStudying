package com.example.AppStudying.services;

import com.example.AppStudying.model.ChatMessage;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.ChatMessageRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatMessage enviarMensagem(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new IllegalStateException("Você não pode enviar uma mensagem para si mesmo!");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("A mensagem não pode estar vazia!");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("Usuário remetente não encontrado!"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalStateException("Usuário destinatário não encontrado!"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        ChatMessage salva = chatMessageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/chat/" + receiverId, salva);
        messagingTemplate.convertAndSend("/topic/chat/" + senderId, salva);

        return salva;
    }

    public List<ChatMessage> listarConversa(Long userId1, Long userId2) {
        return chatMessageRepository.findConversa(userId1, userId2);
    }
}
