package com.example.AppStudying.webSocketConfig;

import com.example.AppStudying.enums.RequestStatus;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.GroupMemberRepository;
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
import java.util.Set;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // Tópicos onde o número no fim da URL é o id de quem está se inscrevendo
    // (dono do tópico) — precisa bater com o próprio usuário autenticado.
    private static final Set<String> PREFIXOS_POR_USUARIO = Set.of(
            "/topic/study-requests/", "/topic/chat/", "/topic/chat-inbox/", "/topic/group-invites/"
    );

    // Tópicos onde o número no fim da URL é o id do GRUPO, não de um usuário
    // — a checagem certa aqui é "faz parte do grupo?", não "é o meu id?".
    private static final Set<String> PREFIXOS_POR_GRUPO = Set.of(
            "/topic/group-presence/", "/topic/group-chat/"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Long trailingId = extractTrailingId(destination);

            if (trailingId != null && comecaComAlgumPrefixo(destination, PREFIXOS_POR_USUARIO)) {
                User authenticatedUser = usuarioAutenticado(accessor);
                if (!authenticatedUser.getId().equals(trailingId)) {
                    throw new AccessDeniedException("Você não pode se inscrever no tópico de outro usuário!");
                }
            } else if (trailingId != null && comecaComAlgumPrefixo(destination, PREFIXOS_POR_GRUPO)) {
                User authenticatedUser = usuarioAutenticado(accessor);
                boolean membroAceito = groupMemberRepository.findByGroupIdAndUserId(trailingId, authenticatedUser.getId())
                        .filter(membro -> membro.getStatus() == RequestStatus.ACEITA)
                        .isPresent();
                if (!membroAceito) {
                    throw new AccessDeniedException("Você não tem permissão para acessar esse grupo!");
                }
            }
        }

        return message;
    }

    private User usuarioAutenticado(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new AccessDeniedException("É necessário estar autenticado para se inscrever nesse tópico!");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado!"));
    }

    private boolean comecaComAlgumPrefixo(String destination, Set<String> prefixos) {
        return prefixos.stream().anyMatch(destination::startsWith);
    }

    private Long extractTrailingId(String destination) {
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
