package com.example.AppStudying.controllers;

import com.example.AppStudying.dto.GroupMemberPresenceDTO;
import com.example.AppStudying.dto.GroupSummaryDTO;
import com.example.AppStudying.model.Group;
import com.example.AppStudying.model.GroupMember;
import com.example.AppStudying.model.GroupMessage;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public Group criarGrupo(@RequestParam String name) {
        return groupService.criarGrupo(CurrentUser.id(), name);
    }

    @GetMapping("/meus/{userId}")
    public List<GroupSummaryDTO> listarMeusGrupos(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        return groupService.listarMeusGrupos(userId);
    }

    @GetMapping("/{id}")
    public GroupSummaryDTO buscarResumo(@PathVariable Long id) {
        return groupService.buscarResumo(id, CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    public void excluirGrupo(@PathVariable Long id) {
        groupService.excluirGrupo(id, CurrentUser.id());
    }

    @PutMapping("/{id}/sair")
    public void sair(@PathVariable Long id) {
        groupService.sair(id, CurrentUser.id());
    }

    @PostMapping("/{id}/convidar")
    public GroupMember convidar(@PathVariable Long id, @RequestParam Long userId) {
        return groupService.convidar(id, CurrentUser.id(), userId);
    }

    @DeleteMapping("/{id}/membros/{userId}")
    public void removerMembro(@PathVariable Long id, @PathVariable Long userId) {
        groupService.removerMembro(id, userId, CurrentUser.id());
    }

    @GetMapping("/{id}/presenca")
    public List<GroupMemberPresenceDTO> listarPresenca(@PathVariable Long id) {
        return groupService.listarPresenca(id, CurrentUser.id());
    }

    @GetMapping("/{id}/convites-pendentes")
    public List<GroupMember> listarConvitesPendentesDoGrupo(@PathVariable Long id) {
        return groupService.listarConvitesPendentesDoGrupo(id, CurrentUser.id());
    }

    @GetMapping("/convites/{userId}")
    public List<GroupMember> listarConvites(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        return groupService.listarConvites(userId);
    }

    @PutMapping("/convites/{id}/aceitar")
    public GroupMember aceitarConvite(@PathVariable Long id) {
        return groupService.aceitarConvite(id, CurrentUser.id());
    }

    @PutMapping("/convites/{id}/recusar")
    public GroupMember recusarConvite(@PathVariable Long id) {
        return groupService.recusarConvite(id, CurrentUser.id());
    }

    @PostMapping("/{id}/mensagens")
    public GroupMessage enviarMensagem(@PathVariable Long id, @RequestParam String content) {
        return groupService.enviarMensagem(id, CurrentUser.id(), content);
    }

    @GetMapping("/{id}/mensagens")
    public List<GroupMessage> listarMensagens(@PathVariable Long id) {
        return groupService.listarMensagens(id, CurrentUser.id());
    }
}
