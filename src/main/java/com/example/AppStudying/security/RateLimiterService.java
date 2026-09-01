package com.example.AppStudying.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Limitador de taxa simples, em memória — suficiente porque a aplicação roda
 * numa única instância (sem múltiplas réplicas atrás de um load balancer).
 * Se algum dia escalar horizontalmente, isso precisa virar algo compartilhado
 * (ex: Redis) para valer entre instâncias.
 */
@Component
public class RateLimiterService {

    private final Map<String, CopyOnWriteArrayList<Instant>> tentativasPorChave = new ConcurrentHashMap<>();

    public boolean permitir(String chave, int maxTentativas, Duration janela) {
        Instant agora = Instant.now();
        CopyOnWriteArrayList<Instant> tentativas =
                tentativasPorChave.computeIfAbsent(chave, k -> new CopyOnWriteArrayList<>());

        tentativas.removeIf(t -> t.isBefore(agora.minus(janela)));

        if (tentativas.size() >= maxTentativas) {
            return false;
        }

        tentativas.add(agora);
        return true;
    }
}
