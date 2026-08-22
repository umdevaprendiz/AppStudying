package com.example.AppStudying.services;

import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email já está cadastrado!");
        }

        if (userRepository.existsByCpf(user.getCpf())) {
            throw new IllegalStateException("Cpf já está cadastrado!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
        }

        public User buscarPorId(Long id){
         return userRepository.findById(id)
                 .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));
        }

        public User buscarPorEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));
        }

        public User autenticar(String email, String senha) {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));

            if (!passwordEncoder.matches(senha, user.getPassword())) {
                throw new IllegalStateException("Senha incorreta!");
            }

            return user;
        }

        public User atualizarUsuario(Long id, String novoNome, String novoEmail){
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


