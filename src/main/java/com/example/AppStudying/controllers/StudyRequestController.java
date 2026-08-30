package com.example.AppStudying.controllers;

import com.example.AppStudying.model.StudyRequest;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.StudyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-requests")
public class StudyRequestController {

    @Autowired
    private StudyRequestService studyRequestService;

    @PostMapping
    public StudyRequest enviarSolicitacao(@RequestParam Long receiverId,
                                           @RequestParam(required = false) Long matterId,
                                           @RequestParam(required = false) String message) {
        return studyRequestService.enviarSolicitacao(CurrentUser.id(), receiverId, matterId, message);
    }

    @PutMapping("/{id}/aceitar")
    public StudyRequest aceitarSolicitacao(@PathVariable Long id) {
        return studyRequestService.aceitarSolicitacao(id, CurrentUser.id());
    }

    @PutMapping("/{id}/recusar")
    public StudyRequest recusarSolicitacao(@PathVariable Long id) {
        return studyRequestService.recusarSolicitacao(id, CurrentUser.id());
    }

    @GetMapping("/recebidas/{userId}")
    public List<StudyRequest> listarRecebidas(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        return studyRequestService.listarRecebidas(userId);
    }

    @GetMapping("/enviadas/{userId}")
    public List<StudyRequest> listarEnviadas(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        return studyRequestService.listarEnviadas(userId);
    }

    @GetMapping("/{id}")
    public StudyRequest buscarPorId(@PathVariable Long id) {
        StudyRequest studyRequest = studyRequestService.buscarPorId(id);
        Long currentUserId = CurrentUser.id();
        if (!studyRequest.getRequester().getId().equals(currentUserId)
                && !studyRequest.getReceiver().getId().equals(currentUserId)) {
            throw new IllegalStateException("Você não tem permissão para ver essa solicitação!");
        }
        return studyRequest;
    }
}
