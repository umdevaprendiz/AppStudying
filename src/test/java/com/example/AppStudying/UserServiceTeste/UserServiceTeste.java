package com.example.AppStudying.UserServiceTeste;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.security.RateLimiterService;
import com.example.AppStudying.services.EmailService;
import com.example.AppStudying.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTeste {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    // Spy (instância real, não mock puro): queremos o comportamento genuíno
    // de janela deslizante pra testar o limite de reenvio de verdade, não
    // simular a resposta.
    @Spy
    private RateLimiterService rateLimiter = new RateLimiterService();

    @Test
    void deveRegistrarUsuarioComSucesso(){
        User user = new User();
        user.setEmail("teste@email.com");
        user.setCpf("12345678900");
        user.setPassword("senha123");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByCpf(user.getCpf())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");
        when(userRepository.save(user)).thenReturn(user);

        User resultado = userService.registerUser(user);

        assertNotNull(resultado);
        assertEquals("senhaCriptografada", resultado.getPassword());

        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, times(1)).existsByCpf(user.getCpf());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado(){
        User user = new User();
        user.setEmail("teste@email.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            userService.registerUser(user);
        });

        verify(userRepository, never()).existsByCpf(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaCadastrado(){
        User user = new User();
        user.setEmail("teste@email.com");
        user.setCpf("12345678900");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByCpf(user.getCpf())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            userService.registerUser(user);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveBuscarUsuarioPorIdComSucesso(){
        Long id = 1L;
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User resultado = userService.buscarPorId(id);

        assertEquals(id, resultado.getId());
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorId(){
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            userService.buscarPorId(id);
        });

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void deveBuscarUsuarioPorEmailComSucesso(){
        String email = "teste@email.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User resultado = userService.buscarPorEmail(email);

        assertEquals(email, resultado.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorEmail(){
        String email = "teste@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            userService.buscarPorEmail(email);
        });

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void deveAtualizarUsuarioComSucesso(){
        Long id = 1L;
        String novoNome = "Novo Nome";
        String novoEmail = "novo@email.com";

        User user = new User();
        user.setId(id);
        user.setName("Nome Antigo");
        user.setEmail("antigo@email.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User resultado = userService.atualizarUsuario(id, novoNome, novoEmail, id);

        assertEquals(novoNome, resultado.getName());
        assertEquals(novoEmail, resultado.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente(){
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            userService.atualizarUsuario(id, "Nome", "email@email.com", id);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioDeOutraPessoa(){
        Long id = 1L;
        Long currentUserId = 2L;

        assertThrows(IllegalStateException.class, () -> {
            userService.atualizarUsuario(id, "Nome", "email@email.com", currentUserId);
        });

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveAlterarSenhaComSucesso(){
        Long id = 1L;
        String senhaAtual = "senhaAtual";
        String novaSenha = "novaSenha";

        User user = new User();
        user.setId(id);
        user.setPassword("senhaCriptografadaAtual");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(senhaAtual, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(novaSenha)).thenReturn("novaSenhaCriptografada");

        userService.alterarSenha(id, senhaAtual, novaSenha);

        assertEquals("novaSenhaCriptografada", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deveListarSugestoesExcluindoUsuarioAtual(){
        Long userId = 1L;

        User self = new User();
        self.setId(userId);

        User outro1 = new User();
        outro1.setId(2L);

        User outro2 = new User();
        outro2.setId(3L);

        when(userRepository.findAll()).thenReturn(List.of(self, outro1, outro2));

        List<User> resultado = userService.listarSugestoes(userId, 10);

        assertEquals(2, resultado.size());
        assertFalse(resultado.stream().anyMatch(u -> u.getId().equals(userId)));
    }

    @Test
    void deveLimitarSugestoesAoLimiteInformado(){
        Long userId = 1L;

        User self = new User();
        self.setId(userId);

        User outro1 = new User();
        outro1.setId(2L);

        User outro2 = new User();
        outro2.setId(3L);

        User outro3 = new User();
        outro3.setId(4L);

        when(userRepository.findAll()).thenReturn(List.of(self, outro1, outro2, outro3));

        List<User> resultado = userService.listarSugestoes(userId, 2);

        assertEquals(2, resultado.size());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaAtualIncorreta(){
        Long id = 1L;
        String senhaAtual = "senhaErrada";

        User user = new User();
        user.setId(id);
        user.setPassword("senhaCriptografadaAtual");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(senhaAtual, user.getPassword())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> {
            userService.alterarSenha(id, senhaAtual, "novaSenha");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveReenviarVerificacaoComSucesso(){
        String email = "naoverificado@email.com";
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.reenviarVerificacao(email);

        assertNotNull(user.getVerificationToken());
        verify(userRepository, times(1)).save(user);
        verify(emailService, times(1)).enviarEmailVerificacao(eq(email), anyString());
    }

    @Test
    void deveLancarExcecaoAoReenviarParaUsuarioJaVerificado(){
        String email = "verificado@email.com";
        User user = new User();
        user.setEmail(email);
        user.setVerified(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> {
            userService.reenviarVerificacao(email);
        });

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).enviarEmailVerificacao(any(), any());
    }

    @Test
    void deveLancarExcecaoAoExcederLimiteDeReenvio(){
        String email = "spam@email.com";
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // As 3 primeiras tentativas (o limite) devem passar sem erro.
        userService.reenviarVerificacao(email);
        userService.reenviarVerificacao(email);
        userService.reenviarVerificacao(email);

        // A 4ª, dentro da mesma janela de tempo, deve ser bloqueada.
        assertThrows(IllegalStateException.class, () -> {
            userService.reenviarVerificacao(email);
        });

        verify(emailService, times(3)).enviarEmailVerificacao(any(), any());
    }

    @Test
    void deveVerificarEmailComSucesso(){
        String token = "token-valido";
        User user = new User();
        user.setVerified(false);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.verificarEmail(token);

        assertTrue(user.getVerified());
        assertNull(user.getVerificationToken());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deveLancarExcecaoAoVerificarTokenInvalido(){
        String token = "token-inexistente";

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            userService.verificarEmail(token);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoVerificarTokenExpirado(){
        String token = "token-expirado";
        User user = new User();
        user.setVerified(false);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> {
            userService.verificarEmail(token);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveIgnorarVerificacaoDeEmailJaVerificado(){
        String token = "token-de-conta-ja-verificada";
        User user = new User();
        user.setVerified(true);
        user.setVerificationToken(token);

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        userService.verificarEmail(token);

        verify(userRepository, never()).save(any(User.class));
    }

}