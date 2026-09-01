package com.example.AppStudying.services;

import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountCleanupScheduler {

    private static final int DIAS_INATIVIDADE = 30;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountDeletionService accountDeletionService;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgarContasInativas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_INATIVIDADE);
        List<Long> idsInativos = userRepository.findIdsInativosDesde(limite);
        for (Long id : idsInativos) {
            accountDeletionService.excluirConta(id);
        }
    }
}
