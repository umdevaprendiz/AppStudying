package com.example.AppStudying.services;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import com.example.AppStudying.security.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final int VERIFICATION_TOKEN_VALID_HOURS = 24;
    private static final int REENVIO_MAX_TENTATIVAS = 3;
    private static final Duration REENVIO_JANELA = Duration.ofHours(1);
    private static final int LOGIN_MAX_TENTATIVAS = 5;
    private static final Duration LOGIN_JANELA = Duration.ofMinutes(15);
    private static final int EXCLUSAO_MAX_TENTATIVAS = 3;
    private static final Duration EXCLUSAO_JANELA = Duration.ofHours(1);
    private static final int EXCLUSAO_TOKEN_VALID_HOURS = 1;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RateLimiterService rateLimiter;
    @Autowired
    private AccountDeletionService accountDeletionService;

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

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Nunca confia em "verified" vindo do cliente: toda conta nova começa
        // não verificada, com um token de confirmação de vida curta.
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
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
        // Verifica o limite antes de tocar no banco: sem isso, esse endpoint
        // público (sem login) podia ser chamado sem limite pra spammar a
        // caixa de entrada de alguém ou estourar a cota de e-mails da conta.
        if (!rateLimiter.permitir("reenviar-verificacao:" + email.toLowerCase(), REENVIO_MAX_TENTATIVAS, REENVIO_JANELA)) {
            throw new IllegalStateException("Muitas tentativas de reenvio. Aguarde um pouco antes de tentar de novo.");
        }

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

        // Chamado antes de tentar autenticar: sem isso, /api/users/login é
        // público e sem limite, então dava pra tentar senha por força bruta
        // contra a conta de qualquer e-mail.
        public void verificarLimiteDeLogin(String email) {
        if (!rateLimiter.permitir("login:" + email.toLowerCase(), LOGIN_MAX_TENTATIVAS, LOGIN_JANELA)) {
            throw new IllegalStateException("Muitas tentativas de login. Aguarde um pouco antes de tentar de novo.");
        }
        }

        // Chamado após autenticar com sucesso: registra a atividade pra
        // alimentar o expurgo automático de contas inativas.
        @Transactional
        public User registrarLogin(String email) {
        User user = buscarPorEmail(email);
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
        }

        @Transactional
        public void solicitarExclusaoConta(Long userId) {
        if (!rateLimiter.permitir("solicitar-exclusao:" + userId, EXCLUSAO_MAX_TENTATIVAS, EXCLUSAO_JANELA)) {
            throw new IllegalStateException("Muitas tentativas de exclusão. Aguarde um pouco antes de tentar de novo.");
        }

        User user = buscarPorId(userId);
        String token = UUID.randomUUID().toString();
        user.setDeletionToken(token);
        user.setDeletionTokenExpiry(LocalDateTime.now().plusHours(EXCLUSAO_TOKEN_VALID_HOURS));
        userRepository.save(user);
        emailService.enviarEmailConfirmacaoExclusao(user.getEmail(), token);
        }

        @Transactional
        public void confirmarExclusaoConta(String token) {
        User user = userRepository.findByDeletionToken(token)
                .orElseThrow(() -> new IllegalStateException("Link de exclusão inválido."));

        if (user.getDeletionTokenExpiry() == null || user.getDeletionTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Link de exclusão expirado. Solicite novamente em Configurações.");
        }

        accountDeletionService.excluirConta(user.getId());
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


