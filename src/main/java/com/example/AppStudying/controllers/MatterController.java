package com.example.AppStudying.controllers;

import com.example.AppStudying.model.Matter;
import com.example.AppStudying.repository.MatterRepository;
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
    public Matter criar(@RequestBody Matter matter, @RequestParam Long userId){
        return matterService.criarMatter(matter, userId);
    }

    @GetMapping({"/{id}"})
    public Matter buscarPorId(@PathVariable Long id){
        return matterService.buscarPorId(id);
    }

    @GetMapping("/user/{userId}")
    public List<Matter> listarPorUsuario(@PathVariable Long userId){
        return matterService.listarPorUsuario(userId);
    }


}
