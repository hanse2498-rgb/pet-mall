-- 售后单（最简版：整单、审核通过即退款）
CREATE TABLE IF NOT EXISTS `tb_after_sale` (
  `id`            BIGINT       NOT NULL COMMENT '主键',
  `after_sale_no` VARCHAR(32)  NOT NULL COMMENT '售后单号',
  `order_id`      BIGINT       NOT NULL COMMENT '订单id',
  `user_id`       BIGINT       NOT NULL COMMENT '用户id',
  `reason`        VARCHAR(64)  DEFAULT NULL COMMENT '售后原因',
  `description`   VARCHAR(255) DEFAULT NULL COMMENT '补充说明',
  `amount`        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '退款金额',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝 3已撤销',
  `admin_remark`  VARCHAR(255) DEFAULT NULL COMMENT '管理员备注',
  `audit_time`    DATETIME     DEFAULT NULL,
  `refund_time`   DATETIME     DEFAULT NULL,
  `create_time`   DATETIME     NOT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单';
