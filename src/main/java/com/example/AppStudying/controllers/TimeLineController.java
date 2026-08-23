package com.example.AppStudying.controllers;

import com.example.AppStudying.model.TimeLine;
import com.example.AppStudying.services.TimeLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/TimeLine")
public class TimeLineController {

    @Autowired
    private TimeLineService timeLineService;

    @PostMapping("/criarEvento")
    public TimeLine criarEvento(@RequestParam String description, @RequestParam Long userId){
        return timeLineService.criarEvento(description, userId);
    }

    @GetMapping("/listarUsuarios")
    public List<TimeLine> listarPorUsuario(@RequestParam Long userId){
        return timeLineService.listarPorUsuario(userId);
    }

    @GetMapping("/{id}")
    public TimeLine buscarPorId(@PathVariable Long id){
        return timeLineService.buscarPorId(id);
    }


}
