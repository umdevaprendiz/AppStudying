package com.example.AppStudying.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensagem("Recurso não encontrado.")));
    }

    // Mais específico que AuthenticationException — o Spring resolve pelo
    // handler mais próximo na hierarquia, então isso tem prioridade sobre o
    // handler genérico abaixo quando o login falha por conta não verificada
    // (CustomUserDetails.isEnabled() == false).
    // Bots de internet vasculhando rotas conhecidas com o método errado
    // (ex: POST em endpoint que só aceita GET) geram isso constantemente em
    // qualquer app público — não é uma falha da aplicação, então não sobe
    // pro handler genérico como erro 500 nem loga stack trace completo.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        log.warn("Método não suportado: {}", ex.getMessage());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Método não permitido para esse endpoint.")));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Confirme seu e-mail antes de fazer login. Verifique sua caixa de entrada.")));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        // A checagem de status usa sempre a mensagem original em português
        // (statusParaMensagem casa palavras-chave em PT) — só a mensagem que
        // vai pro corpo da resposta é traduzida.
        HttpStatus status = statusParaMensagem(ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensagem(ex.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Já existe um registro com esses dados.")));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Email ou senha inválidos.")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Você não tem permissão para acessar esse recurso.")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Sem isso, qualquer exceção não mapeada vira um 500 genérico e some
        // — impossível de investigar em produção sem olhar o stack trace.
        log.error("Erro inesperado não tratado", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensagem("Ocorreu um erro inesperado. Tente novamente mais tarde.")));
    }

    // Traduz a mensagem final pro idioma da requisição (header Accept-Language,
    // enviado pelo frontend a partir do idioma escolhido na interface — ver
    // ErrorMessageTranslations). Sem tradução cadastrada, devolve o
    // português original.
    private String mensagem(String mensagemOriginal) {
        return ErrorMessageTranslations.translate(mensagemOriginal, LocaleContextHolder.getLocale().getLanguage());
    }

    private HttpStatus statusParaMensagem(String mensagem) {
        if (mensagem == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (mensagem.contains("não encontrado")) {
            return HttpStatus.NOT_FOUND;
        }
        if (mensagem.contains("permissão")) {
            return HttpStatus.FORBIDDEN;
        }
        if (mensagem.contains("já está cadastrado") || mensagem.contains("já existe")) {
            return HttpStatus.CONFLICT;
        }
        if (mensagem.contains("Muitas tentativas")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
