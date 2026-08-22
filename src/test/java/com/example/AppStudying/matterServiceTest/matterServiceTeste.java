package com.example.AppStudying.matterServiceTest;

import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.MatterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class matterServiceTeste {

    @Mock
    private MatterRepository matterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatterService matterService;

    @Test
    void TesteDeCriaçãoSucessoQuandoUsuárioExistir() {
        Long userId = 1L;
        User user = new User();
        Matter matter = new Matter();
        matter.setNome("Cálculo 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.existsByNomeAndUserId("Cálculo1", userId)).thenReturn(false);
        when(matterRepository.save(matter)).thenReturn(matter);

        Matter resultado = matterService.criarMatter(matter, userId);

        assertEquals(user, resultado.getUser());
        verify(matterRepository, times(1)).save(matter);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {


    }

}



