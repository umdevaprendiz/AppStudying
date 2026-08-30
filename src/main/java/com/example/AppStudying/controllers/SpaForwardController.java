package com.example.AppStudying.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Em produção o frontend (React Router, com histórico de navegador via
 * BrowserRouter) é servido pelo próprio Spring a partir de resources/static.
 * O Spring só sabe responder em "/" (welcome page) e em arquivos que
 * realmente existem em static/**; uma URL de rota do React acessada direto
 * (ex: recarregar a página em /dashboard) cairia em 404 sem isso, porque só
 * o JavaScript já carregado sabe rotear "/dashboard" no client-side.
 * Encaminhamos qualquer caminho sem extensão (ou seja, sem ".") de volta pro
 * index.html, deixando o React Router assumir o roteamento a partir daí.
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {"/{path:[^.]*}", "/**/{path:[^.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
