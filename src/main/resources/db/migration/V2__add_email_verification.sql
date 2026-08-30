-- Suporte à verificação de e-mail no cadastro: conta nasce não verificada,
-- com um token de confirmação de vida curta enviado por e-mail.
ALTER TABLE `users`
    ADD COLUMN `verified` bit(1) NOT NULL DEFAULT 0,
    ADD COLUMN `verification_token` varchar(255) DEFAULT NULL,
    ADD COLUMN `verification_token_expiry` datetime(6) DEFAULT NULL;

CREATE INDEX `idx_users_verification_token` ON `users` (`verification_token`);
