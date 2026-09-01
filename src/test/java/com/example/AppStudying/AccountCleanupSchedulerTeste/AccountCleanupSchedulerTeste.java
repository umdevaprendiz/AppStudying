package com.example.AppStudying.AccountCleanupSchedulerTeste;

import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.AccountCleanupScheduler;
import com.example.AppStudying.services.AccountDeletionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountCleanupSchedulerTeste {

    @InjectMocks
    private AccountCleanupScheduler accountCleanupScheduler;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountDeletionService accountDeletionService;

    @Test
    void devePurgarTodasAsContasInativasEncontradas(){
        when(userRepository.findIdsInativosDesde(any(LocalDateTime.class)))
                .thenReturn(List.of(1L, 2L, 3L));

        accountCleanupScheduler.purgarContasInativas();

        verify(accountDeletionService, times(1)).excluirConta(1L);
        verify(accountDeletionService, times(1)).excluirConta(2L);
        verify(accountDeletionService, times(1)).excluirConta(3L);
    }

    @Test
    void naoDeveExcluirNadaQuandoNaoHaContasInativas(){
        when(userRepository.findIdsInativosDesde(any(LocalDateTime.class)))
                .thenReturn(List.of());

        accountCleanupScheduler.purgarContasInativas();

        verify(accountDeletionService, never()).excluirConta(any());
    }

    @Test
    void deveUsarLimiteDeTrintaDiasDeInatividade(){
        ArgumentCaptor<LocalDateTime> limiteCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(userRepository.findIdsInativosDesde(limiteCaptor.capture())).thenReturn(List.of());

        LocalDateTime antes = LocalDateTime.now().minusDays(30);
        accountCleanupScheduler.purgarContasInativas();
        LocalDateTime depois = LocalDateTime.now().minusDays(30);

        LocalDateTime limiteUsado = limiteCaptor.getValue();
        assertTrue(!limiteUsado.isBefore(antes) && !limiteUsado.isAfter(depois));
    }
}
