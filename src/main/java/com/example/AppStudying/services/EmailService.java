package com.example.AppStudying.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // URL pública do frontend (a mesma origem do backend em produção); o link
    // do e-mail leva pra rota /verify-email, que o React Router trata.
    @Value("${app.mail.verification-base-url}")
    private String verificationBaseUrl;

    // Sem isso, o JavaMail monta um remetente "padrão" a partir do usuário e
    // do IP da máquina local (ex: "usuario"@192.168.0.x) quando nenhum From é
    // definido — o Gmail rejeita isso com "555 Syntax error, cannot decode
    // response" porque não é um endereço válido.
    @Value("${spring.mail.username}")
    private String remetente;

    public void enviarEmailVerificacao(String destinatario, String token) {
        String link = verificationBaseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(destinatario);
        message.setSubject("Confirme seu e-mail - AppStudying");
        message.setText(
                "Bem-vindo(a) ao AppStudying!\n\n" +
                "Clique no link abaixo para confirmar seu e-mail e ativar sua conta:\n" +
                link + "\n\n" +
                "Esse link expira em 24 horas. Se você não criou essa conta, ignore este e-mail."
        );

        mailSender.send(message);
    }
}
