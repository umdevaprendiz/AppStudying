package com.example.AppStudying.security;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTeste {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User criarUsuario(String email, String cpf, String senha) {
        User user = new User();
        user.setName("Usuário Teste");
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPassword(passwordEncoder.encode(senha));
        return userRepository.save(user);
    }

    private MockCookie login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .param("email", email)
                        .param("senha", senha))
                .andExpect(status().isOk())
                .andReturn();

        MockCookie cookie = (MockCookie) result.getResponse().getCookie("SESSION");
        assertNotNull(cookie, "Login deveria criar um cookie de sessão");
        return cookie;
    }

    @Test
    void deveBloquearAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/matters/user/1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deveRejeitarLoginComSenhaErrada() throws Exception {
        User user = criarUsuario("seguranca.senha@teste.com", "10000000001", "senha123");

        mockMvc.perform(post("/api/users/login")
                        .param("email", user.getEmail())
                        .param("senha", "senhaErrada"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void devePermitirLoginEAcessarOProprioRecurso() throws Exception {
        User user = criarUsuario("seguranca.proprio@teste.com", "10000000002", "senha123");

        MockCookie sessionCookie = login(user.getEmail(), "senha123");

        mockMvc.perform(get("/api/matters/user/" + user.getId()).cookie(sessionCookie))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAcessoAoRecursoDeOutroUsuario() throws Exception {
        User user = criarUsuario("seguranca.a@teste.com", "10000000003", "senha123");
        User outro = criarUsuario("seguranca.b@teste.com", "10000000004", "senha123");

        MockCookie sessionCookie = login(user.getEmail(), "senha123");

        // MockMvc não converte exceções não tratadas em resposta HTTP (diferente de um Tomcat real),
        // então aqui verificamos que a exceção de permissão foi de fato lançada pelo controller.
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/matters/user/" + outro.getId()).cookie(sessionCookie))
        );

        Throwable raiz = exception;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        org.junit.jupiter.api.Assertions.assertTrue(raiz instanceof IllegalStateException);
        org.junit.jupiter.api.Assertions.assertTrue(raiz.getMessage().contains("permissão"));
    }

    @Test
    void naoDeveExporSenhaNaRespostaDoLogin() throws Exception {
        User user = criarUsuario("seguranca.senhaoculta@teste.com", "10000000005", "senha123");

        mockMvc.perform(post("/api/users/login")
                        .param("email", user.getEmail())
                        .param("senha", "senha123"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.junit.jupiter.api.Assertions.assertFalse(
                            body.contains("senha123") || body.toLowerCase().contains("\"password\""),
                            "A resposta do login não deveria conter a senha"
                    );
                });
    }
}
