package com.example.AppStudying.webSocketConfig;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Long targetUserId = extractTrailingUserId(destination);

            if (targetUserId != null) {
                Principal principal = accessor.getUser();
                if (principal == null) {
                    throw new AccessDeniedException("É necessário estar autenticado para se inscrever nesse tópico!");
                }

                User authenticatedUser = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado!"));

                if (!authenticatedUser.getId().equals(targetUserId)) {
                    throw new AccessDeniedException("Você não pode se inscrever no tópico de outro usuário!");
                }
            }
        }

        return message;
    }

    private Long extractTrailingUserId(String destination) {
        if (destination == null) {
            return null;
        }
        int lastSlash = destination.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == destination.length() - 1) {
            return null;
        }
        try {
            return Long.parseLong(destination.substring(lastSlash + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
