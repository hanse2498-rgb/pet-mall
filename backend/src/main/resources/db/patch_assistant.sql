-- 问答助手：建表
USE `mall`;

CREATE TABLE IF NOT EXISTS `tb_chat_message` (
  `id`          BIGINT        NOT NULL,
  `user_id`     BIGINT        NOT NULL,
  `role`        VARCHAR(16)   NOT NULL,
  `content`     VARCHAR(2000) NOT NULL,
  `create_time` DATETIME      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答消息';