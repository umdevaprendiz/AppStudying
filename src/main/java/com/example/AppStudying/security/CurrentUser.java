package com.example.AppStudying.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("Usuário não autenticado!");
    }

    public static void requireSelf(Long userId) {
        if (!id().equals(userId)) {
            throw new IllegalStateException("Você não tem permissão para acessar esse recurso!");
        }
    }
}
