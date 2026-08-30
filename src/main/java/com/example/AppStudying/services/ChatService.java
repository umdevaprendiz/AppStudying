package com.example.AppStudying.services;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.ChatMessage;
import com.example.AppStudying.model.Conversation;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.ChatMessageRepository;
import com.example.AppStudying.repository.ConversationRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

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

        Conversation conversation = conversationRepository.findEntreUsuarios(senderId, receiverId)
                .orElse(null);

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setRequester(sender);
            conversation.setReceiver(receiver);
            conversation.setStatus(RequestStatus.PENDENTE);
            conversation.setCreatedAt(LocalDateTime.now());
        } else if (conversation.getStatus() == RequestStatus.RECUSADA) {
            throw new IllegalStateException("Essa conversa foi recusada e não pode receber novas mensagens.");
        } else if (conversation.getStatus() == RequestStatus.PENDENTE
                && conversation.getReceiver().getId().equals(senderId)) {
            conversation.setStatus(RequestStatus.ACEITA);
        }

        conversation.setLastMessageAt(LocalDateTime.now());
        conversation = conversationRepository.save(conversation);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        ChatMessage salva = chatMessageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/chat/" + receiverId, salva);
        messagingTemplate.convertAndSend("/topic/chat/" + senderId, salva);
        messagingTemplate.convertAndSend("/topic/chat-inbox/" + receiverId, contarNaoLidas(receiverId));

        return salva;
    }

    @Transactional
    public List<ChatMessage> listarConversa(Long selfId, Long otherId) {
        List<ChatMessage> mensagens = chatMessageRepository.findConversa(selfId, otherId);
        chatMessageRepository.marcarComoLidas(selfId, otherId);
        messagingTemplate.convertAndSend("/topic/chat-inbox/" + selfId, contarNaoLidas(selfId));
        return mensagens;
    }

    public List<Conversation> listarSolicitacoes(Long userId) {
        return conversationRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDENTE);
    }

    public List<Conversation> listarConversasAceitas(Long userId) {
        return conversationRepository.findPorUsuarioEStatus(userId, RequestStatus.ACEITA);
    }

    public Conversation aceitarConversa(Long conversationId) {
        Conversation conversation = buscarConversaPorId(conversationId);

        if (conversation.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Essa conversa já foi respondida!");
        }

        conversation.setStatus(RequestStatus.ACEITA);
        Conversation salva = conversationRepository.save(conversation);
        avisarAtualizacaoContagem(salva.getReceiver().getId());
        return salva;
    }

    public Conversation recusarConversa(Long conversationId) {
        Conversation conversation = buscarConversaPorId(conversationId);

        if (conversation.getStatus() != RequestStatus.PENDENTE) {
            throw new IllegalStateException("Essa conversa já foi respondida!");
        }

        conversation.setStatus(RequestStatus.RECUSADA);
        Conversation salva = conversationRepository.save(conversation);
        avisarAtualizacaoContagem(salva.getReceiver().getId());
        return salva;
    }

    private void avisarAtualizacaoContagem(Long userId) {
        messagingTemplate.convertAndSend("/topic/chat-inbox/" + userId, contarNaoLidas(userId));
    }

    public Conversation buscarConversaPorId(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversa não encontrada!"));
    }

    public long contarNaoLidas(Long userId) {
        long solicitacoesPendentes = conversationRepository.countByReceiverIdAndStatus(userId, RequestStatus.PENDENTE);
        long mensagensNaoLidas = chatMessageRepository.countByReceiverIdAndReadFalse(userId);
        return solicitacoesPendentes + mensagensNaoLidas;
    }
}
