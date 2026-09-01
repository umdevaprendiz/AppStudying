package com.example.AppStudying.controllers;

import com.example.AppStudying.enums.TypeStatus;
import com.example.AppStudying.model.Topic;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @PostMapping
    public Topic criar(@RequestBody Topic topic, @RequestParam Long matterId) {
        return topicService.criarTopic(topic, matterId, CurrentUser.id());
    }

    @GetMapping("/{id}")
    public Topic buscarPorId(@PathVariable Long id) {
        return topicService.buscarPorId(id, CurrentUser.id());
    }

    @GetMapping("/matter/{matterId}")
    public List<Topic> listarPorMatter(@PathVariable Long matterId) {
        return topicService.listarPorMatter(matterId, CurrentUser.id());
    }

    @PutMapping("/{id}")
    public Topic atualizar(@PathVariable Long id,
                            @RequestParam String novoNome,
                            @RequestParam TypeStatus novoStatus) {
        return topicService.atualizarTopic(id, novoNome, novoStatus, CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        topicService.excluirTopic(id, CurrentUser.id());
    }
}
