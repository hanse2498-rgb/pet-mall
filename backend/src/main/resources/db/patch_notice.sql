-- 站内通知消息（订单 / 售后）
CREATE TABLE IF NOT EXISTS `tb_notice` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1订单 2售后',
  `title`       VARCHAR(128) NOT NULL COMMENT '标题',
  `content`     VARCHAR(512) DEFAULT NULL COMMENT '内容',
  `biz_id`      BIGINT       DEFAULT NULL COMMENT '关联订单id（用于跳转）',
  `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` DATETIME     NOT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';
