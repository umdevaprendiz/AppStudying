package com.example.AppStudying.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Envia e-mail via API HTTP da Brevo (https://api.brevo.com/v3/smtp/email),
 * não por SMTP direto. A maioria dos provedores de hospedagem grátis
 * (Render incluso, desde set/2025) bloqueia as portas SMTP de saída
 * (25/465/587) pra evitar abuso de spam — a API roda sobre HTTPS (443), que
 * não é bloqueada.
 */
@Service
public class EmailService {

    private final WebClient webClient;

    @Value("${app.mail.verification-base-url}")
    private String verificationBaseUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    // Precisa ser um remetente verificado na conta Brevo (Settings > Senders).
    @Value("${brevo.sender.email}")
    private String remetente;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.brevo.com/v3").build();
    }

    public void enviarEmailVerificacao(String destinatario, String token) {
        String link = verificationBaseUrl + "/verify-email?token=" + token;
        String texto =
                "Bem-vindo(a) ao AppStudying!\n\n" +
                "Clique no link abaixo para confirmar seu e-mail e ativar sua conta:\n" +
                link + "\n\n" +
                "Esse link expira em 24 horas. Se você não criou essa conta, ignore este e-mail.";

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", remetente, "name", "AppStudying"),
                "to", List.of(Map.of("email", destinatario)),
                "subject", "Confirme seu e-mail - AppStudying",
                "textContent", texto
        );

        webClient.post()
                .uri("/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void enviarEmailConfirmacaoExclusao(String destinatario, String token) {
        String link = verificationBaseUrl + "/delete-account?token=" + token;
        String texto =
                "Recebemos um pedido para excluir sua conta no AppStudying.\n\n" +
                "Se foi você, clique no link abaixo para confirmar. Essa ação é " +
                "irreversível e apaga todos os seus dados (matérias, tópicos, " +
                "sessões de estudo, conversas):\n" +
                link + "\n\n" +
                "Esse link expira em 1 hora. Se você não pediu isso, ignore este e-mail — " +
                "sua conta continua normalmente.";

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", remetente, "name", "AppStudying"),
                "to", List.of(Map.of("email", destinatario)),
                "subject", "Confirme a exclusão da sua conta - AppStudying",
                "textContent", texto
        );

        webClient.post()
                .uri("/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
