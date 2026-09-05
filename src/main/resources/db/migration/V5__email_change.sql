-- Suporte a troca de e-mail com confirmacao (mesmo padrao do cadastro e da
-- exclusao de conta): o e-mail so muda de fato depois que a pessoa clica no
-- link enviado para o e-mail NOVO.
ALTER TABLE `users`
    ADD COLUMN `pending_email` varchar(255) DEFAULT NULL,
    ADD COLUMN `email_change_token` varchar(255) DEFAULT NULL,
    ADD COLUMN `email_change_token_expiry` datetime(6) DEFAULT NULL;

CREATE INDEX `idx_users_email_change_token` ON `users` (`email_change_token`);
