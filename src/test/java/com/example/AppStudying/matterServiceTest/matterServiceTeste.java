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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
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
    void deveCriarMatterComSucessoQuandoUsuarioExiste() {
        Long userId = 1L;
        User user = new User();
        Matter matter = new Matter();
        matter.setNome("Cálculo 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matterRepository.existsByNomeAndUserId("Cálculo 1", userId)).thenReturn(false);
        when(matterRepository.save(matter)).thenReturn(matter);

        Matter resultado = matterService.criarMatter(matter, userId);

        assertEquals(user, resultado.getUser());
        verify(matterRepository, times(1)).save(matter);
    }

    @Test
    void SucessoMateriaExistir(){
        Long id = 99L;
        Matter matter = new Matter();
        matter.setNome("Programação");

        when(matterRepository.findById(id)).thenReturn(Optional.of(matter));

        Matter resultado = matterService.buscarPorId(id);

        assertEquals("Programação", resultado.getNome());
        verify(matterRepository, times(1)).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoMateriaNaoExiste() {
        Long id = 1L;

        when(matterRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            matterService.buscarPorId(id);
        });

        verify(matterRepository, times(1)).findById(id);

    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {

        Long userId = 1L;
        Matter matter = new Matter();
        matter.setNome("Cálculo 1");


        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            matterService.criarMatter(matter, userId);
        });

        verify(userRepository, times(1)).findById(userId);
        verify(matterRepository, never()).save(matter);
    }

    @Test
    void deveListarMateriasPorUsuario(){
        Long userId = 1L;

        Matter matter1 = new Matter();
        matter1.setNome("Cálculo 1");

        Matter matter2 = new Matter();
        matter2.setNome("Programação");

        when(matterRepository.findByUserId(userId)).thenReturn(List.of(matter1, matter2));

        List<Matter> resultado = matterService.listarPorUsuario(userId);

        assertEquals(2, resultado.size());
        verify(matterRepository, times(1)).findByUserId(userId);
    }

    //Concluído.

}



