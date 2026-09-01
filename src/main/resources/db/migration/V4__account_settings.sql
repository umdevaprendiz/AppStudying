-- Suporte a configuracoes de conta: exclusao de conta (com confirmacao por
-- e-mail, mesmo padrao do cadastro) e expurgo automatico de contas inativas.
ALTER TABLE `users`
    ADD COLUMN `created_at` datetime(6) DEFAULT NULL,
    ADD COLUMN `last_login_at` datetime(6) DEFAULT NULL,
    ADD COLUMN `deletion_token` varchar(255) DEFAULT NULL,
    ADD COLUMN `deletion_token_expiry` datetime(6) DEFAULT NULL;

CREATE INDEX `idx_users_deletion_token` ON `users` (`deletion_token`);
