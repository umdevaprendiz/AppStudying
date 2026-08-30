package com.example.AppStudying.controllers;

import com.example.AppStudying.model.Matter;
import com.example.AppStudying.security.CurrentUser;
import com.example.AppStudying.services.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matters")
public class MatterController {
    @Autowired
    private MatterService matterService;

    @PostMapping
    public Matter criar(@RequestBody Matter matter){
        return matterService.criarMatter(matter, CurrentUser.id());
    }

    @GetMapping({"/{id}"})
    public Matter buscarPorId(@PathVariable Long id){
        Matter matter = matterService.buscarPorId(id);
        CurrentUser.requireSelf(matter.getUser().getId());
        return matter;
    }

    @GetMapping("/user/{userId}")
    public List<Matter> listarPorUsuario(@PathVariable Long userId){
        CurrentUser.requireSelf(userId);
        return matterService.listarPorUsuario(userId);
    }


}
