package com.example.AppStudying.controllers;

import com.example.AppStudying.model.User;
import com.example.AppStudying.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/registrarUser")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping("/buscarUser/{email}")
    public User buscarPorEmail(@PathVariable String email) {
        return userService.buscarPorEmail(email);
    }
    @GetMapping("/{id}")
    public User buscarPorId(@PathVariable Long id) {
        return userService.buscarPorId(id);
    }

    @PostMapping("/login")
    public User autenticar(@RequestParam String email, @RequestParam String senha) {
        return userService.autenticar(email, senha);
    }

    @PutMapping("/{id}")
    public User atualizarUsuario(@PathVariable Long id, @RequestParam String novoNome, @RequestParam String email) {
        return userService.atualizarUsuario(id, novoNome, email);
    }


}
