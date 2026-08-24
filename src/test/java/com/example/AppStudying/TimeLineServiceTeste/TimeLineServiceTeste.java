package com.example.AppStudying.TimeLineServiceTeste;

import com.example.AppStudying.model.TimeLine;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.TimeLineRepository;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.TimeLineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimeLineServiceTeste {

    @Mock
    private TimeLineRepository timeLineRepository;

    @InjectMocks
    private TimeLineService timeLineService;

    @Mock
    private UserRepository userRepository;

    @Test
    void criarEventoSucesso(){
        Long userId = 1L;
        String description = "Iniciando cálculo 2";

        User user = new User();
        user.setId(userId);

        TimeLine timeLineSalva = new TimeLine();
        timeLineSalva.setId(10L);
        timeLineSalva.setUser(user);
        timeLineSalva.setDescription(description);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeLineRepository.save(any(TimeLine.class))).thenReturn(timeLineSalva);

        TimeLine resultado = timeLineService.criarEvento(description, userId);

        assertNotNull(resultado);
        assertEquals(description, resultado.getDescription());
        assertEquals(user, resultado.getUser());

        verify(userRepository, times(1)).findById(userId);
        verify(timeLineRepository, times(1)).save(any(TimeLine.class));

    }

    @Test
    void criarEventoUsuarioNaoEncontrado(){
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->{
            timeLineService.criarEvento("Anythings", userId);
        });

        verify(timeLineRepository, never()).save(any(TimeLine.class));

    }

    @Test
    void listarPorUsuarioRetornaLista(){
        Long userId = 1L;

        TimeLine timeLine1 = new TimeLine();
        timeLine1.setId(1L);
        timeLine1.setDescription("Estudo cálculo 1");

        TimeLine timeLine2 = new TimeLine();
        timeLine2.setId(2L);
        timeLine2.setDescription("Estudando Programação");
        when(timeLineRepository.findByUserId(userId)).thenReturn(List.of(timeLine1, timeLine2 ));

        List<TimeLine> resultado = timeLineService.listarPorUsuario(userId);

        assertEquals(2, resultado.size());
        verify(timeLineRepository, times(1)).findByUserId(userId);

    }

    @Test
    void buscarIdNaoEncontrado(){
        Long id = 88L; // Criamos um id aleatório
        TimeLine timeLine = new TimeLine(); //Criamos novos parâmetros
        timeLine.setId(id);
        timeLine.setDescription("Estudou Cálculo 1");

        when(timeLineRepository.findById(id)).thenReturn(Optional.empty()); //Quando buscar por id (88) o retorno será vazio.
        assertThrows(IllegalStateException.class, () -> {
            timeLineService.buscarPorId(id);
        });

        verify(timeLineRepository, times(1)).findById(id);
    }

    @Test
    void buscarIdSucesso(){
        Long id = 10L;
        TimeLine timeLine = new TimeLine();
        timeLine.setDescription("Estudando Cálculo 2");
        timeLine.setId(id);

        when(timeLineRepository.findById(id)).thenReturn(Optional.of(timeLine));

        TimeLine resultado = timeLineService.buscarPorId(id);

        assertEquals("Estudando Cálculo 2", resultado.getDescription());
        verify(timeLineRepository, times(1)).findById(id);

    }
}
