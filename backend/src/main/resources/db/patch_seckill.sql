-- 秒杀专区：建表
USE `mall`;

CREATE TABLE IF NOT EXISTS `tb_seckill_activity` (
  `id`          BIGINT      NOT NULL,
  `name`        VARCHAR(64) NOT NULL,
  `start_time`  DATETIME    NOT NULL,
  `end_time`    DATETIME    NOT NULL,
  `status`      TINYINT     NOT NULL DEFAULT 0,
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀场次';

CREATE TABLE IF NOT EXISTS `tb_seckill_item` (
  `id`             BIGINT        NOT NULL,
  `activity_id`    BIGINT        NOT NULL,
  `sku_id`         BIGINT        NOT NULL,
  `seckill_price`  DECIMAL(10,2) NOT NULL,
  `seckill_stock`  INT           NOT NULL DEFAULT 0,
  `seckill_sold`   INT           NOT NULL DEFAULT 0,
  `per_user_limit` INT           NOT NULL DEFAULT 1,
  `status`         TINYINT       NOT NULL DEFAULT 1,
  `sort`           INT           NOT NULL DEFAULT 0,
  `create_time`    DATETIME      NOT NULL,
  `update_time`    DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品项';

CREATE TABLE IF NOT EXISTS `tb_seckill_record` (
  `id`          BIGINT   NOT NULL,
  `user_id`     BIGINT   NOT NULL,
  `item_id`     BIGINT   NOT NULL,
  `quantity`    INT      NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
  KEY `idx_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀购买记录';