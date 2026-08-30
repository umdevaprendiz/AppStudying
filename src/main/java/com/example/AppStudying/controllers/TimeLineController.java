package com.example.AppStudying.controllers;

import com.example.AppStudying.model.TimeLine;
import com.example.AppStudying.security.CurrentUser;
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
    public TimeLine criarEvento(@RequestParam String description){
        return timeLineService.criarEvento(description, CurrentUser.id());
    }

    @GetMapping("/listarUsuarios")
    public List<TimeLine> listarPorUsuario(@RequestParam Long userId){
        CurrentUser.requireSelf(userId);
        return timeLineService.listarPorUsuario(userId);
    }

    @GetMapping("/{id}")
    public TimeLine buscarPorId(@PathVariable Long id){
        TimeLine timeLine = timeLineService.buscarPorId(id);
        CurrentUser.requireSelf(timeLine.getUser().getId());
        return timeLine;
    }


}
