-- Baseline gerado a partir do schema que o Hibernate (ddl-auto=create) produzia
-- automaticamente antes de introduzirmos o Flyway. A partir daqui, toda mudança
-- de schema deve vir como um novo arquivo V{n}__descricao.sql nesta pasta —
-- nunca mais editar este arquivo depois que ele rodar em algum ambiente.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `SPRING_SESSION` (
  `PRIMARY_ID` char(36) NOT NULL,
  `SESSION_ID` char(36) NOT NULL,
  `CREATION_TIME` bigint NOT NULL,
  `LAST_ACCESS_TIME` bigint NOT NULL,
  `MAX_INACTIVE_INTERVAL` int NOT NULL,
  `EXPIRY_TIME` bigint NOT NULL,
  `PRINCIPAL_NAME` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`PRIMARY_ID`),
  UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
  KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
  KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `SPRING_SESSION_ATTRIBUTES` (
  `SESSION_PRIMARY_ID` char(36) NOT NULL,
  `ATTRIBUTE_NAME` varchar(200) NOT NULL,
  `ATTRIBUTE_BYTES` blob NOT NULL,
  PRIMARY KEY (`SESSION_PRIMARY_ID`,`ATTRIBUTE_NAME`),
  CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `SPRING_SESSION` (`PRIMARY_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cpf` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7kqluf7wl0oxs7n90fpya03ss` (`cpf`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `matter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `name_matter` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj4ynev8cr1tdp6a9t2kh23y45` (`user_id`),
  CONSTRAINT `FKj4ynev8cr1tdp6a9t2kh23y45` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `topic` (
  `status` tinyint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `matter_id` bigint DEFAULT NULL,
  `nome` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt6tr6tnottwjl9x20rrlgjadh` (`matter_id`),
  CONSTRAINT `FKt6tr6tnottwjl9x20rrlgjadh` FOREIGN KEY (`matter_id`) REFERENCES `matter` (`id`),
  CONSTRAINT `topic_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `conversation` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_message_at` datetime(6) DEFAULT NULL,
  `receiver_id` bigint DEFAULT NULL,
  `requester_id` bigint DEFAULT NULL,
  `status` enum('ACEITA','PENDENTE','RECUSADA') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfs4apdd01nmuvo9mfag6ycwml` (`receiver_id`),
  KEY `FKerbmhewnmmuw6itnmfrynyx2a` (`requester_id`),
  CONSTRAINT `FKerbmhewnmmuw6itnmfrynyx2a` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfs4apdd01nmuvo9mfag6ycwml` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_message` (
  `is_read` bit(1) DEFAULT NULL,
  `conversation_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint DEFAULT NULL,
  `sender_id` bigint DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `content` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkxe2b8q35d0baph3krucvraif` (`conversation_id`),
  KEY `FK46s59psfyf53qh0ayebjnn6tu` (`receiver_id`),
  KEY `FK5f82aoyy0jiwpj08qapfrxbh6` (`sender_id`),
  CONSTRAINT `FK46s59psfyf53qh0ayebjnn6tu` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK5f82aoyy0jiwpj08qapfrxbh6` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKkxe2b8q35d0baph3krucvraif` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `study_request` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `matter_id` bigint DEFAULT NULL,
  `receiver_id` bigint DEFAULT NULL,
  `requester_id` bigint DEFAULT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `status` enum('ACEITA','PENDENTE','RECUSADA') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmqkm9yd2udulwy6jn3nmplx6` (`matter_id`),
  KEY `FKhehdv6h90t7619p0t7qu0jbo0` (`receiver_id`),
  KEY `FKbh1l4b9ha4ic4ciylwquat1mx` (`requester_id`),
  CONSTRAINT `FKbh1l4b9ha4ic4ciylwquat1mx` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhehdv6h90t7619p0t7qu0jbo0` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKmqkm9yd2udulwy6jn3nmplx6` FOREIGN KEY (`matter_id`) REFERENCES `matter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `study_session` (
  `fim` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `inicio` datetime(6) DEFAULT NULL,
  `matter_id` bigint DEFAULT NULL,
  `topic_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3y0hmj4uo2g2ck04xn49385r` (`matter_id`),
  KEY `FKkfojempgo0h9h9abubyxnt1hw` (`topic_id`),
  KEY `FK3bkr0f6m9dbckmbh3yt4dcpwd` (`user_id`),
  CONSTRAINT `FK3bkr0f6m9dbckmbh3yt4dcpwd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK3y0hmj4uo2g2ck04xn49385r` FOREIGN KEY (`matter_id`) REFERENCES `matter` (`id`),
  CONSTRAINT `FKkfojempgo0h9h9abubyxnt1hw` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `time_line` (
  `event_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKri95t0sfuppocamt765hym45m` (`user_id`),
  CONSTRAINT `FKri95t0sfuppocamt765hym45m` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
