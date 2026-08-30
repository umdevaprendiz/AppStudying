package com.example.AppStudying.controllers;

import com.example.AppStudying.model.ChatMessage;
import com.example.AppStudying.model.Conversation;
import com.example.AppStudying.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ChatMessage enviarMensagem(@RequestParam Long senderId,
                                       @RequestParam Long receiverId,
                                       @RequestParam String content) {
        return chatService.enviarMensagem(senderId, receiverId, content);
    }

    @GetMapping("/conversa")
    public List<ChatMessage> listarConversa(@RequestParam Long userId1, @RequestParam Long userId2) {
        return chatService.listarConversa(userId1, userId2);
    }

    @GetMapping("/solicitacoes/{userId}")
    public List<Conversation> listarSolicitacoes(@PathVariable Long userId) {
        return chatService.listarSolicitacoes(userId);
    }

    @GetMapping("/conversas/{userId}")
    public List<Conversation> listarConversasAceitas(@PathVariable Long userId) {
        return chatService.listarConversasAceitas(userId);
    }

    @PutMapping("/conversas/{id}/aceitar")
    public Conversation aceitarConversa(@PathVariable Long id) {
        return chatService.aceitarConversa(id);
    }

    @PutMapping("/conversas/{id}/recusar")
    public Conversation recusarConversa(@PathVariable Long id) {
        return chatService.recusarConversa(id);
    }

    @GetMapping("/contagem/{userId}")
    public long contarNaoLidas(@PathVariable Long userId) {
        return chatService.contarNaoLidas(userId);
    }
}
