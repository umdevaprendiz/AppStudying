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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions. assertNotNull;
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

}
