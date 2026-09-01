package com.example.AppStudying.controllers;

import com.example.AppStudying.model.User;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/sugestoes")
    public List<User> listarSugestoes(@RequestParam(defaultValue = "10") int limite) {
        return userService.listarSugestoes(CurrentUser.id(), limite);
    }

    @PostMapping("/registrarUser")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping("/verificar-email")
    public void verificarEmail(@RequestParam String token) {
        userService.verificarEmail(token);
    }

    @PostMapping("/reenviar-verificacao")
    public void reenviarVerificacao(@RequestParam String email) {
        userService.reenviarVerificacao(email);
    }

    @GetMapping("/buscarUser/{email}")
    public User buscarPorEmail(@PathVariable String email) {
        return userService.buscarPorEmail(email);
    }

    @GetMapping("/{id}")
    public User buscarPorId(@PathVariable Long id) {
        return userService.buscarPorId(id);
    }

    @PostMapping("/login")
    public User autenticar(@RequestParam String email, @RequestParam String senha, HttpServletRequest request) {
        userService.verificarLimiteDeLogin(email);

        Authentication authRequest = new UsernamePasswordAuthenticationToken(email, senha);
        Authentication authResult = authenticationManager.authenticate(authRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return userService.buscarPorEmail(email);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @PutMapping("/{id}")
    public User atualizarUsuario(@PathVariable Long id, @RequestParam String novoNome, @RequestParam String email) {
        return userService.atualizarUsuario(id, novoNome, email, CurrentUser.id());
    }
}
