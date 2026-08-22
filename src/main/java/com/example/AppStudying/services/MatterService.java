package com.example.AppStudying.services;

import com.example.AppStudying.model.Matter;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.MatterRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatterService {

    @Autowired
    private MatterRepository matterRepository;

    @Autowired
    private UserRepository userRepository;

    public Matter criarMatter(Matter matter, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));

        if (matterRepository.existsByNomeAndUserId(matter.getNome(), userId)) {
            throw new IllegalStateException("Você já possui matéria com esse nome!");
        }

        matter.setUser(user);
        return matterRepository.save(matter);
    }

    @Cacheable
    public Matter buscarPorId(Long id) {
        return matterRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Matéria não encontrada"));
    }

    public List<Matter> listarPorUsuario(Long userId) {
        return matterRepository.findByUserId(userId);
    }

}
