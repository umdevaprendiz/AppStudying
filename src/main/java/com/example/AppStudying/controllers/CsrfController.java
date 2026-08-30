package com.example.AppStudying.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public void csrf(CsrfToken token) {
        // O simples fato de resolver CsrfToken como parâmetro força o
        // CookieCsrfTokenRepository a gravar o cookie XSRF-TOKEN na resposta.
        // O frontend chama esse endpoint uma vez ao carregar para garantir
        // que o cookie exista antes da primeira requisição que muda estado.
    }
}
