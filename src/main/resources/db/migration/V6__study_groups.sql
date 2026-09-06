-- Grupos de estudo: uma sala com ate 30 membros (limite aplicado na service,
-- nao no schema), convite/aceite reaproveitando o mesmo enum de status usado
-- em study_request e conversation, e um mural de mensagens proprio (nao reusa
-- chat_message porque aquela tabela e estritamente 1:1 entre requester/receiver).
CREATE TABLE `study_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `owner_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_study_groups_owner` (`owner_id`),
  CONSTRAINT `FK_study_groups_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `invited_by_id` bigint DEFAULT NULL,
  `status` enum('ACEITA','PENDENTE','RECUSADA') DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  -- Uma linha por par (grupo, usuario): reenviar convite so reativa a linha
  -- existente (ver GroupService), nunca duplica.
  UNIQUE KEY `UK_group_member_group_user` (`group_id`, `user_id`),
  KEY `FK_group_member_user` (`user_id`),
  KEY `FK_group_member_invited_by` (`invited_by_id`),
  CONSTRAINT `FK_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `study_groups` (`id`),
  CONSTRAINT `FK_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK_group_member_invited_by` FOREIGN KEY (`invited_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint DEFAULT NULL,
  `sender_id` bigint DEFAULT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_group_message_group` (`group_id`),
  KEY `FK_group_message_sender` (`sender_id`),
  CONSTRAINT `FK_group_message_group` FOREIGN KEY (`group_id`) REFERENCES `study_groups` (`id`),
  CONSTRAINT `FK_group_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
