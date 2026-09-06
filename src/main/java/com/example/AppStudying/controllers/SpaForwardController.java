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
 * Encaminhamos essas rotas de volta pro index.html, deixando o React Router
 * assumir o roteamento a partir daí.
 *
 * Antes isso usava um catch-all ("qualquer caminho sem ponto"), mas sem
 * nenhum @RequestMapping concorrente pra "/ws/**", esse catch-all também
 * capturava a negociação do SockJS (/ws/info, /ws/{server}/{session}/xhr...
 * — que também não tem ponto no último segmento) e encaminhava ela pra
 * index.html por engano, quebrando o WebSocket. Tentar excluir prefixos
 * reservados via regex esbarrou em limitações reais do casamento de padrão
 * do Spring (PathPatternParser não aceita lookahead; já com AntPathMatcher,
 * duas variáveis nomeadas coladas não casam certo com rotas de dois
 * segmentos). A lista explícita abaixo evita as duas armadilhas: precisa
 * adicionar uma rota nova aqui sempre que uma rota nova for adicionada no
 * React Router (App.jsx), mas nunca vai colidir com um endpoint do backend.
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/verify-email",
            "/delete-account",
            "/confirmar-troca-email",
            "/dashboard",
            "/settings",
            "/groups",
            "/groups/{id}",
            "/profile/{userId}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
