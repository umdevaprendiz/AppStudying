package com.example.AppStudying.exception;

import java.util.Map;

/**
 * O backend lança e loga suas mensagens de erro em português (é o idioma
 * nativo do código, dos logs e dos testes). Esta classe só traduz a
 * mensagem final que vai pro cliente, na borda ({@link GlobalExceptionHandler}),
 * usando a mensagem original em português como chave de busca — assim nenhum
 * service precisa saber que a interface tem múltiplos idiomas.
 * <p>
 * O idioma vem do header HTTP {@code Accept-Language}, que o frontend
 * define manualmente a cada requisição a partir do idioma escolhido pela
 * pessoa (ver {@code frontend/src/api/http.js}), resolvido automaticamente
 * pelo {@code AcceptHeaderLocaleResolver} padrão do Spring.
 * <p>
 * Sem tradução cadastrada pro idioma pedido (ou idioma desconhecido), a
 * mensagem original em português é devolvida — nunca quebra por falta de
 * uma entrada nova aqui.
 */
public final class ErrorMessageTranslations {

    private ErrorMessageTranslations() {
    }

    public static String translate(String mensagemOriginal, String idioma) {
        if (mensagemOriginal == null || idioma == null) {
            return mensagemOriginal;
        }
        Map<String, String> dicionario = switch (idioma) {
            case "en" -> EN;
            case "es" -> ES;
            default -> null;
        };
        if (dicionario == null) {
            return mensagemOriginal;
        }
        return dicionario.getOrDefault(mensagemOriginal, mensagemOriginal);
    }

    private static final Map<String, String> EN = Map.ofEntries(
            Map.entry("A mensagem não pode estar vazia!", "The message cannot be empty!"),
            Map.entry("A senha deve ter pelo menos 8 caracteres.", "The password must be at least 8 characters long."),
            Map.entry("Conversa não encontrada!", "Conversation not found!"),
            Map.entry("Convite não encontrado!", "Invite not found!"),
            Map.entry("E-mail inválido.", "Invalid email."),
            Map.entry("Email já está cadastrado!", "Email is already registered!"),
            Map.entry("Essa conta já está verificada.", "This account is already verified."),
            Map.entry("Essa conversa foi recusada e não pode receber novas mensagens.", "This conversation was declined and can no longer receive new messages."),
            Map.entry("Essa conversa já foi respondida!", "This conversation has already been answered!"),
            Map.entry("Essa pessoa já faz parte do grupo!", "This person is already a member of the group!"),
            Map.entry("Essa pessoa já tem um convite pendente para esse grupo!", "This person already has a pending invite to this group!"),
            Map.entry("Essa pessoa não faz parte desse grupo!", "This person is not a member of this group!"),
            Map.entry("Essa solicitação já foi respondida!", "This request has already been answered!"),
            Map.entry("Esse convite já foi respondido!", "This invite has already been answered!"),
            Map.entry("Esse e-mail já está em uso.", "This email is already in use."),
            Map.entry("Esse grupo já atingiu o limite de 30 membros!", "This group has already reached the 30-member limit!"),
            Map.entry("Esse já é o seu e-mail atual.", "That's already your current email."),
            Map.entry("Grupo não encontrado!", "Group not found!"),
            Map.entry("Link de exclusão expirado. Solicite novamente em Configurações.", "Deletion link expired. Request it again in Settings."),
            Map.entry("Link de exclusão inválido.", "Invalid deletion link."),
            Map.entry("Link de troca de e-mail expirado. Solicite novamente em Configurações.", "Email change link expired. Request it again in Settings."),
            Map.entry("Link de troca de e-mail inválido.", "Invalid email change link."),
            Map.entry("Link de verificação expirado. Solicite um novo.", "Verification link expired. Request a new one."),
            Map.entry("Link de verificação inválido.", "Invalid verification link."),
            Map.entry("Matéria não encontrada!", "Subject not found!"),
            Map.entry("Matéria não encontrada", "Subject not found"),
            Map.entry("Muitas tentativas de cadastro. Aguarde um pouco antes de tentar de novo.", "Too many sign-up attempts. Wait a bit before trying again."),
            Map.entry("Muitas tentativas de exclusão. Aguarde um pouco antes de tentar de novo.", "Too many deletion attempts. Wait a bit before trying again."),
            Map.entry("Muitas tentativas de login. Aguarde um pouco antes de tentar de novo.", "Too many login attempts. Wait a bit before trying again."),
            Map.entry("Muitas tentativas de reenvio. Aguarde um pouco antes de tentar de novo.", "Too many resend attempts. Wait a bit before trying again."),
            Map.entry("Muitas tentativas de troca de e-mail. Aguarde um pouco antes de tentar de novo.", "Too many email change attempts. Wait a bit before trying again."),
            Map.entry("O dono não pode sair do grupo. Exclua o grupo se quiser encerrá-lo.", "The owner can't leave the group. Delete the group if you want to end it."),
            Map.entry("O grupo precisa de um nome!", "The group needs a name!"),
            Map.entry("Senha atual incorreta!", "Current password is incorrect!"),
            Map.entry("Sessão de estudos não encontrada!", "Study session not found!"),
            Map.entry("Sessão de estudos não encontrado!", "Study session not found!"),
            Map.entry("Sessão não encontrada!", "Session not found!"),
            Map.entry("Solicitação não encontrada!", "Request not found!"),
            Map.entry("Sua sessão ainda não foi encerrada!", "Your session hasn't been ended yet!"),
            Map.entry("Sua sessão já foi encerrada!", "Your session has already been ended!"),
            Map.entry("Só o dono pode excluir esse grupo!", "Only the owner can delete this group!"),
            Map.entry("TimeLine não encontrado", "Timeline event not found"),
            Map.entry("Tópico não encontrado!", "Topic not found!"),
            Map.entry("Tópico não encontrado", "Topic not found"),
            Map.entry("Usuário convidado não encontrado!", "Invited user not found!"),
            Map.entry("Usuário destinatário não encontrado!", "Recipient user not found!"),
            Map.entry("Usuário não autenticado!", "User not authenticated!"),
            Map.entry("Usuário não encontrado!", "User not found!"),
            Map.entry("Usuário não encontrado", "User not found"),
            Map.entry("Usuário remetente não encontrado!", "Sender user not found!"),
            Map.entry("Usuário solicitante não encontrado!", "Requesting user not found!"),
            Map.entry("Você já possui matéria com esse nome!", "You already have a subject with this name!"),
            Map.entry("Você não faz parte desse grupo!", "You are not a member of this group!"),
            Map.entry("Você não pode convidar a si mesmo!", "You can't invite yourself!"),
            Map.entry("Você não pode enviar uma mensagem para si mesmo!", "You can't send a message to yourself!"),
            Map.entry("Você não pode enviar uma solicitação para si mesmo!", "You can't send a request to yourself!"),
            Map.entry("Você não pode remover a si mesmo. Exclua o grupo se quiser encerrá-lo.", "You can't remove yourself. Delete the group if you want to end it."),
            Map.entry("Você não pode se inscrever no tópico de outro usuário!", "You can't subscribe to another user's topic!"),
            Map.entry("Você não tem permissão para acessar essa sessão de estudos!", "You don't have permission to access this study session!"),
            Map.entry("Você não tem permissão para acessar esse grupo!", "You don't have permission to access this group!"),
            Map.entry("Você não tem permissão para acessar esse recurso!", "You don't have permission to access this resource!"),
            Map.entry("Você não tem permissão para acessar esse tópico!", "You don't have permission to access this topic!"),
            Map.entry("Você não tem permissão para adicionar tópicos a essa matéria!", "You don't have permission to add topics to this subject!"),
            Map.entry("Você não tem permissão para atualizar esse usuário!", "You don't have permission to update this user!"),
            Map.entry("Você não tem permissão para remover membros desse grupo!", "You don't have permission to remove members from this group!"),
            Map.entry("Você não tem permissão para responder essa conversa!", "You don't have permission to respond to this conversation!"),
            Map.entry("Você não tem permissão para responder essa solicitação!", "You don't have permission to respond to this request!"),
            Map.entry("Você não tem permissão para responder esse convite!", "You don't have permission to respond to this invite!"),
            Map.entry("Você não tem permissão para ver essa solicitação!", "You don't have permission to view this request!"),
            Map.entry("Você não tem permissão para ver os tópicos dessa matéria!", "You don't have permission to view this subject's topics!"),
            Map.entry("É necessário estar autenticado para se inscrever nesse tópico!", "You must be authenticated to subscribe to this topic!"),
            Map.entry("Recurso não encontrado.", "Resource not found."),
            Map.entry("Método não permitido para esse endpoint.", "Method not allowed for this endpoint."),
            Map.entry("Confirme seu e-mail antes de fazer login. Verifique sua caixa de entrada.", "Confirm your email before logging in. Check your inbox."),
            Map.entry("Já existe um registro com esses dados.", "A record with this data already exists."),
            Map.entry("Email ou senha inválidos.", "Invalid email or password."),
            Map.entry("Você não tem permissão para acessar esse recurso.", "You don't have permission to access this resource."),
            Map.entry("Ocorreu um erro inesperado. Tente novamente mais tarde.", "An unexpected error occurred. Please try again later.")
    );

    private static final Map<String, String> ES = Map.ofEntries(
            Map.entry("A mensagem não pode estar vazia!", "¡El mensaje no puede estar vacío!"),
            Map.entry("A senha deve ter pelo menos 8 caracteres.", "La contraseña debe tener al menos 8 caracteres."),
            Map.entry("Conversa não encontrada!", "¡Conversación no encontrada!"),
            Map.entry("Convite não encontrado!", "¡Invitación no encontrada!"),
            Map.entry("E-mail inválido.", "Correo electrónico inválido."),
            Map.entry("Email já está cadastrado!", "¡El correo electrónico ya está registrado!"),
            Map.entry("Essa conta já está verificada.", "Esta cuenta ya está verificada."),
            Map.entry("Essa conversa foi recusada e não pode receber novas mensagens.", "Esta conversación fue rechazada y ya no puede recibir nuevos mensajes."),
            Map.entry("Essa conversa já foi respondida!", "¡Esta conversación ya fue respondida!"),
            Map.entry("Essa pessoa já faz parte do grupo!", "¡Esta persona ya forma parte del grupo!"),
            Map.entry("Essa pessoa já tem um convite pendente para esse grupo!", "¡Esta persona ya tiene una invitación pendiente para este grupo!"),
            Map.entry("Essa pessoa não faz parte desse grupo!", "¡Esta persona no forma parte de este grupo!"),
            Map.entry("Essa solicitação já foi respondida!", "¡Esta solicitud ya fue respondida!"),
            Map.entry("Esse convite já foi respondido!", "¡Esta invitación ya fue respondida!"),
            Map.entry("Esse e-mail já está em uso.", "Este correo electrónico ya está en uso."),
            Map.entry("Esse grupo já atingiu o limite de 30 membros!", "¡Este grupo ya alcanzó el límite de 30 miembros!"),
            Map.entry("Esse já é o seu e-mail atual.", "Ese ya es tu correo electrónico actual."),
            Map.entry("Grupo não encontrado!", "¡Grupo no encontrado!"),
            Map.entry("Link de exclusão expirado. Solicite novamente em Configurações.", "El enlace de eliminación expiró. Solicítalo de nuevo en Configuración."),
            Map.entry("Link de exclusão inválido.", "Enlace de eliminación inválido."),
            Map.entry("Link de troca de e-mail expirado. Solicite novamente em Configurações.", "El enlace de cambio de correo expiró. Solicítalo de nuevo en Configuración."),
            Map.entry("Link de troca de e-mail inválido.", "Enlace de cambio de correo inválido."),
            Map.entry("Link de verificação expirado. Solicite um novo.", "El enlace de verificación expiró. Solicita uno nuevo."),
            Map.entry("Link de verificação inválido.", "Enlace de verificación inválido."),
            Map.entry("Matéria não encontrada!", "¡Materia no encontrada!"),
            Map.entry("Matéria não encontrada", "Materia no encontrada"),
            Map.entry("Muitas tentativas de cadastro. Aguarde um pouco antes de tentar de novo.", "Demasiados intentos de registro. Espera un poco antes de volver a intentarlo."),
            Map.entry("Muitas tentativas de exclusão. Aguarde um pouco antes de tentar de novo.", "Demasiados intentos de eliminación. Espera un poco antes de volver a intentarlo."),
            Map.entry("Muitas tentativas de login. Aguarde um pouco antes de tentar de novo.", "Demasiados intentos de inicio de sesión. Espera un poco antes de volver a intentarlo."),
            Map.entry("Muitas tentativas de reenvio. Aguarde um pouco antes de tentar de novo.", "Demasiados intentos de reenvío. Espera un poco antes de volver a intentarlo."),
            Map.entry("Muitas tentativas de troca de e-mail. Aguarde um pouco antes de tentar de novo.", "Demasiados intentos de cambio de correo. Espera un poco antes de volver a intentarlo."),
            Map.entry("O dono não pode sair do grupo. Exclua o grupo se quiser encerrá-lo.", "El dueño no puede salir del grupo. Elimina el grupo si quieres terminarlo."),
            Map.entry("O grupo precisa de um nome!", "¡El grupo necesita un nombre!"),
            Map.entry("Senha atual incorreta!", "¡Contraseña actual incorrecta!"),
            Map.entry("Sessão de estudos não encontrada!", "¡Sesión de estudio no encontrada!"),
            Map.entry("Sessão de estudos não encontrado!", "¡Sesión de estudio no encontrada!"),
            Map.entry("Sessão não encontrada!", "¡Sesión no encontrada!"),
            Map.entry("Solicitação não encontrada!", "¡Solicitud no encontrada!"),
            Map.entry("Sua sessão ainda não foi encerrada!", "¡Tu sesión todavía no ha terminado!"),
            Map.entry("Sua sessão já foi encerrada!", "¡Tu sesión ya ha terminado!"),
            Map.entry("Só o dono pode excluir esse grupo!", "¡Solo el dueño puede eliminar este grupo!"),
            Map.entry("TimeLine não encontrado", "Evento de la línea de tiempo no encontrado"),
            Map.entry("Tópico não encontrado!", "¡Tema no encontrado!"),
            Map.entry("Tópico não encontrado", "Tema no encontrado"),
            Map.entry("Usuário convidado não encontrado!", "¡Usuario invitado no encontrado!"),
            Map.entry("Usuário destinatário não encontrado!", "¡Usuario destinatario no encontrado!"),
            Map.entry("Usuário não autenticado!", "¡Usuario no autenticado!"),
            Map.entry("Usuário não encontrado!", "¡Usuario no encontrado!"),
            Map.entry("Usuário não encontrado", "Usuario no encontrado"),
            Map.entry("Usuário remetente não encontrado!", "¡Usuario remitente no encontrado!"),
            Map.entry("Usuário solicitante não encontrado!", "¡Usuario solicitante no encontrado!"),
            Map.entry("Você já possui matéria com esse nome!", "¡Ya tienes una materia con ese nombre!"),
            Map.entry("Você não faz parte desse grupo!", "¡No formas parte de este grupo!"),
            Map.entry("Você não pode convidar a si mesmo!", "¡No puedes invitarte a ti mismo!"),
            Map.entry("Você não pode enviar uma mensagem para si mesmo!", "¡No puedes enviarte un mensaje a ti mismo!"),
            Map.entry("Você não pode enviar uma solicitação para si mesmo!", "¡No puedes enviarte una solicitud a ti mismo!"),
            Map.entry("Você não pode remover a si mesmo. Exclua o grupo se quiser encerrá-lo.", "No puedes eliminarte a ti mismo. Elimina el grupo si quieres terminarlo."),
            Map.entry("Você não pode se inscrever no tópico de outro usuário!", "¡No puedes suscribirte al tema de otro usuario!"),
            Map.entry("Você não tem permissão para acessar essa sessão de estudos!", "¡No tienes permiso para acceder a esta sesión de estudio!"),
            Map.entry("Você não tem permissão para acessar esse grupo!", "¡No tienes permiso para acceder a este grupo!"),
            Map.entry("Você não tem permissão para acessar esse recurso!", "¡No tienes permiso para acceder a este recurso!"),
            Map.entry("Você não tem permissão para acessar esse tópico!", "¡No tienes permiso para acceder a este tema!"),
            Map.entry("Você não tem permissão para adicionar tópicos a essa matéria!", "¡No tienes permiso para agregar temas a esta materia!"),
            Map.entry("Você não tem permissão para atualizar esse usuário!", "¡No tienes permiso para actualizar este usuario!"),
            Map.entry("Você não tem permissão para remover membros desse grupo!", "¡No tienes permiso para eliminar miembros de este grupo!"),
            Map.entry("Você não tem permissão para responder essa conversa!", "¡No tienes permiso para responder esta conversación!"),
            Map.entry("Você não tem permissão para responder essa solicitação!", "¡No tienes permiso para responder esta solicitud!"),
            Map.entry("Você não tem permissão para responder esse convite!", "¡No tienes permiso para responder esta invitación!"),
            Map.entry("Você não tem permissão para ver essa solicitação!", "¡No tienes permiso para ver esta solicitud!"),
            Map.entry("Você não tem permissão para ver os tópicos dessa matéria!", "¡No tienes permiso para ver los temas de esta materia!"),
            Map.entry("É necessário estar autenticado para se inscrever nesse tópico!", "¡Es necesario estar autenticado para suscribirse a este tema!"),
            Map.entry("Recurso não encontrado.", "Recurso no encontrado."),
            Map.entry("Método não permitido para esse endpoint.", "Método no permitido para este endpoint."),
            Map.entry("Confirme seu e-mail antes de fazer login. Verifique sua caixa de entrada.", "Confirma tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada."),
            Map.entry("Já existe um registro com esses dados.", "Ya existe un registro con estos datos."),
            Map.entry("Email ou senha inválidos.", "Correo electrónico o contraseña inválidos."),
            Map.entry("Você não tem permissão para acessar esse recurso.", "No tienes permiso para acceder a este recurso."),
            Map.entry("Ocorreu um erro inesperado. Tente novamente mais tarde.", "Ocurrió un error inesperado. Inténtalo de nuevo más tarde.")
    );
}
