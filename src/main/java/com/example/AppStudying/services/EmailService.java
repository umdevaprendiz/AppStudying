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
 * <p>
 * Cada e-mail é enviado no idioma da pessoa no momento da ação (registro,
 * pedido de troca de e-mail, pedido de exclusão) — o mesmo idioma resolvido
 * pelo {@code Accept-Language} da requisição que disparou o envio (ver
 * {@code UserService}), sem precisar guardar preferência de idioma no banco.
 * Idioma não reconhecido cai em inglês, o idioma padrão do app.
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

    private record Conteudo(String assunto, String corpo) {
    }

    public void enviarEmailVerificacao(String destinatario, String token, String idioma) {
        String link = verificationBaseUrl + "/verify-email?token=" + token;
        Conteudo conteudo = switch (idiomaOuPadrao(idioma)) {
            case "pt" -> new Conteudo(
                    "Confirme seu e-mail - AppStudying",
                    "Bem-vindo(a) ao AppStudying!\n\n" +
                    "Clique no link abaixo para confirmar seu e-mail e ativar sua conta:\n" +
                    link + "\n\n" +
                    "Esse link expira em 24 horas. Se você não criou essa conta, ignore este e-mail."
            );
            case "es" -> new Conteudo(
                    "Confirma tu correo electrónico - AppStudying",
                    "¡Bienvenido/a a AppStudying!\n\n" +
                    "Haz clic en el enlace de abajo para confirmar tu correo electrónico y activar tu cuenta:\n" +
                    link + "\n\n" +
                    "Este enlace expira en 24 horas. Si no creaste esta cuenta, ignora este correo."
            );
            default -> new Conteudo(
                    "Confirm your email - AppStudying",
                    "Welcome to AppStudying!\n\n" +
                    "Click the link below to confirm your email and activate your account:\n" +
                    link + "\n\n" +
                    "This link expires in 24 hours. If you didn't create this account, ignore this email."
            );
        };
        enviar(destinatario, conteudo);
    }

    public void enviarEmailTrocaEmail(String destinatario, String token, String idioma) {
        String link = verificationBaseUrl + "/confirmar-troca-email?token=" + token;
        Conteudo conteudo = switch (idiomaOuPadrao(idioma)) {
            case "pt" -> new Conteudo(
                    "Confirme a troca do seu e-mail - AppStudying",
                    "Recebemos um pedido para trocar o e-mail da sua conta no AppStudying para este endereço.\n\n" +
                    "Se foi você, clique no link abaixo para confirmar a troca:\n" +
                    link + "\n\n" +
                    "Esse link expira em 24 horas. Se você não pediu isso, ignore este e-mail — " +
                    "sua conta continua com o e-mail atual."
            );
            case "es" -> new Conteudo(
                    "Confirma el cambio de tu correo electrónico - AppStudying",
                    "Recibimos una solicitud para cambiar el correo electrónico de tu cuenta de AppStudying a esta dirección.\n\n" +
                    "Si fuiste tú, haz clic en el enlace de abajo para confirmar el cambio:\n" +
                    link + "\n\n" +
                    "Este enlace expira en 24 horas. Si no solicitaste esto, ignora este correo — " +
                    "tu cuenta mantiene su correo actual."
            );
            default -> new Conteudo(
                    "Confirm your email change - AppStudying",
                    "We received a request to change your AppStudying account's email to this address.\n\n" +
                    "If this was you, click the link below to confirm the change:\n" +
                    link + "\n\n" +
                    "This link expires in 24 hours. If you didn't request this, ignore this email — " +
                    "your account keeps its current email."
            );
        };
        enviar(destinatario, conteudo);
    }

    public void enviarEmailConfirmacaoExclusao(String destinatario, String token, String idioma) {
        String link = verificationBaseUrl + "/delete-account?token=" + token;
        Conteudo conteudo = switch (idiomaOuPadrao(idioma)) {
            case "pt" -> new Conteudo(
                    "Confirme a exclusão da sua conta - AppStudying",
                    "Recebemos um pedido para excluir sua conta no AppStudying.\n\n" +
                    "Se foi você, clique no link abaixo para confirmar. Essa ação é " +
                    "irreversível e apaga todos os seus dados (matérias, tópicos, " +
                    "sessões de estudo, conversas):\n" +
                    link + "\n\n" +
                    "Esse link expira em 1 hora. Se você não pediu isso, ignore este e-mail — " +
                    "sua conta continua normalmente."
            );
            case "es" -> new Conteudo(
                    "Confirma la eliminación de tu cuenta - AppStudying",
                    "Recibimos una solicitud para eliminar tu cuenta de AppStudying.\n\n" +
                    "Si fuiste tú, haz clic en el enlace de abajo para confirmar. Esta acción es " +
                    "irreversible y borra todos tus datos (materias, temas, " +
                    "sesiones de estudio, conversaciones):\n" +
                    link + "\n\n" +
                    "Este enlace expira en 1 hora. Si no solicitaste esto, ignora este correo — " +
                    "tu cuenta continúa normalmente."
            );
            default -> new Conteudo(
                    "Confirm your account deletion - AppStudying",
                    "We received a request to delete your AppStudying account.\n\n" +
                    "If this was you, click the link below to confirm. This action is " +
                    "irreversible and erases all your data (subjects, topics, " +
                    "study sessions, conversations):\n" +
                    link + "\n\n" +
                    "This link expires in 1 hour. If you didn't request this, ignore this email — " +
                    "your account continues normally."
            );
        };
        enviar(destinatario, conteudo);
    }

    private String idiomaOuPadrao(String idioma) {
        if ("pt".equals(idioma) || "es".equals(idioma)) {
            return idioma;
        }
        return "en";
    }

    private void enviar(String destinatario, Conteudo conteudo) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", remetente, "name", "AppStudying"),
                "to", List.of(Map.of("email", destinatario)),
                "subject", conteudo.assunto(),
                "textContent", conteudo.corpo()
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
