package com.example.AppStudying.controllers;

import com.example.AppStudying.dto.MatterStudySummaryDTO;
import com.example.AppStudying.model.StudySession;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.StudySessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

    @Autowired
    private StudySessionService studySessionService;

    @PostMapping("/iniciar")
    public StudySession iniciarSessao(@RequestParam Long matterId,
                                       @RequestParam(required = false) Long topicId) {
        return studySessionService.iniciarSessao(CurrentUser.id(), matterId, topicId);
    }

    @PutMapping("/{id}/encerrar")
    public StudySession encerrarSessao(@PathVariable Long id) {
        return studySessionService.encerrarSessao(id, CurrentUser.id());
    }

    @GetMapping("/ativa")
    public StudySession buscarSessaoAtiva(@RequestParam Long matterId) {
        return studySessionService.buscarSessaoAtiva(CurrentUser.id(), matterId);
    }

    @GetMapping("/usuario/{userId}")
    public List<StudySession> listarPorUsuario(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        return studySessionService.listarPorUsuario(userId);
    }

    @GetMapping("/{id}")
    public StudySession buscarPorId(@PathVariable Long id) {
        return studySessionService.buscarPorId(id, CurrentUser.id());
    }

    @GetMapping("/{id}/duracao")
    public Long calcularDuracao(@PathVariable Long id) {
        return studySessionService.calcularDuracao(id, CurrentUser.id());
    }

    @GetMapping("/resumo/{userId}")
    public List<MatterStudySummaryDTO> resumoPorUsuario(@PathVariable Long userId) {
        return studySessionService.resumoPorUsuario(userId);
    }
}
