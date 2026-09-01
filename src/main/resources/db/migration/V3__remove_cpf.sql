-- Cadastro simplificado: mantem apenas nome, e-mail e senha.
-- Remover a coluna tambem remove seu indice unico automaticamente.
ALTER TABLE `users`
    DROP COLUMN `cpf`;
