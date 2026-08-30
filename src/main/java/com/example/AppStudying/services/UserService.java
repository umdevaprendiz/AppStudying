package com.example.AppStudying.services;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final int VERIFICATION_TOKEN_VALID_HOURS = 24;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    // Se o envio do e-mail falhar (SMTP fora do ar, credencial errada), o
    // insert do usuário e a geração do token são desfeitos junto — melhor o
    // cadastro falhar de forma limpa (cliente pode tentar de novo com o
    // mesmo e-mail) do que criar uma conta "fantasma" sem e-mail de
    // verificação nenhum.
    @Transactional
    public User registerUser(User user) {
        user.setId(null);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email já está cadastrado!");
        }

        if (userRepository.existsByCpf(user.getCpf())) {
            throw new IllegalStateException("Cpf já está cadastrado!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Nunca confia em "verified" vindo do cliente: toda conta nova começa
        // não verificada, com um token de confirmação de vida curta.
        user.setVerified(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_VALID_HOURS));

        User salvo = userRepository.save(user);
        emailService.enviarEmailVerificacao(salvo.getEmail(), token);
        return salvo;
        }

        public void verificarEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("Link de verificação inválido."));

        if (Boolean.TRUE.equals(user.getVerified())) {
            return;
        }

        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Link de verificação expirado. Solicite um novo.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        }

        @Transactional
        public void reenviarVerificacao(String email) {
        User user = buscarPorEmail(email);

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalStateException("Essa conta já está verificada.");
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_VALID_HOURS));
        userRepository.save(user);
        emailService.enviarEmailVerificacao(user.getEmail(), token);
        }

        public User buscarPorId(Long id){
         return userRepository.findById(id)
                 .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));
        }

        public User buscarPorEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));
        }

        public List<User> listarSugestoes(Long userId, int limite){
        List<User> candidatos = new ArrayList<>(userRepository.findAll());
        candidatos.removeIf(u -> u.getId().equals(userId));
        Collections.shuffle(candidatos);

        if (candidatos.size() > limite) {
            return candidatos.subList(0, limite);
        }
        return candidatos;
        }

        public User atualizarUsuario(Long id, String novoNome, String novoEmail, Long currentUserId){
        if (!id.equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para atualizar esse usuário!");
        }

        User user = buscarPorId(id);
        user.setName(novoNome);
        user.setEmail(novoEmail);
        return userRepository.save(user);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        User user = buscarPorId(id);

        if (!passwordEncoder.matches(senhaAtual, user.getPassword())) {
            throw new IllegalStateException("Senha atual incorreta!");
        }

        user.setPassword(passwordEncoder.encode(novaSenha));
        userRepository.save(user);
        }
    }


