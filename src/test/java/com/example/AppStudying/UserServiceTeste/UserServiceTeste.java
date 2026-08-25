package com.example.AppStudying.UserServiceTeste;

import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.services.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith((MockitoExtension.class))
public class UserServiceTeste {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;


    

}
