-- ============================================================
-- 宠物商城 初始化脚本
-- 数据库：mall  utf8mb4
-- 基于 docs/数据库设计.md
-- ============================================================

CREATE DATABASE IF NOT EXISTS `mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall`;

-- ---------- 用户域 ----------

CREATE TABLE IF NOT EXISTS `tb_user` (
  `id`             BIGINT       NOT NULL COMMENT '主键(雪花)',
  `phone`          VARCHAR(20)  NOT NULL COMMENT '手机号',
  `password_hash`  VARCHAR(128) DEFAULT NULL COMMENT '密码(预留)',
  `nickname`       VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  `avatar`         VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `last_login_time` DATETIME    DEFAULT NULL COMMENT '最后登录时间',
  `create_time`    DATETIME     NOT NULL COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

CREATE TABLE IF NOT EXISTS `tb_user_address` (
  `id`           BIGINT      NOT NULL COMMENT '主键',
  `user_id`      BIGINT      NOT NULL COMMENT '用户id',
  `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人',
  `phone`        VARCHAR(20) NOT NULL COMMENT '收货电话',
  `province`     VARCHAR(32) DEFAULT NULL COMMENT '省',
  `city`         VARCHAR(32) DEFAULT NULL COMMENT '市',
  `district`     VARCHAR(32) DEFAULT NULL COMMENT '区',
  `detail`       VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default`   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认 0否 1是',
  `create_time`  DATETIME    NOT NULL COMMENT '创建时间',
  `update_time`  DATETIME    DEFAULT NULL COMMENT '更新时间',
  `deleted`      TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- ---------- 宠物域 ----------

CREATE TABLE IF NOT EXISTS `tb_pet` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `nickname`    VARCHAR(64)  NOT NULL COMMENT '宠物昵称',
  `species`     VARCHAR(16)  NOT NULL COMMENT '种类：猫/狗/其他',
  `breed`       VARCHAR(64)  DEFAULT NULL COMMENT '品种',
  `birth_date`  DATE         DEFAULT NULL COMMENT '生日',
  `weight_kg`   DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `is_neutered` TINYINT      DEFAULT 0 COMMENT '是否绝育 0否 1是',
  `allergy`     VARCHAR(255) DEFAULT NULL COMMENT '过敏史',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME     NOT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物档案';

-- ---------- 商品域 ----------

CREATE TABLE IF NOT EXISTS `tb_brand` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `name`        VARCHAR(64)  NOT NULL COMMENT '品牌名',
  `logo`        VARCHAR(255) DEFAULT NULL COMMENT 'logo',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME     NOT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌';

CREATE TABLE IF NOT EXISTS `tb_category` (
  `id`          BIGINT      NOT NULL COMMENT '主键',
  `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类，0为一级',
  `name`        VARCHAR(64) NOT NULL COMMENT '分类名',
  `level`       TINYINT     NOT NULL DEFAULT 1 COMMENT '层级',
  `sort`        INT         NOT NULL DEFAULT 0,
  `status`      TINYINT     NOT NULL DEFAULT 1,
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS `tb_product` (
  `id`           BIGINT       NOT NULL COMMENT '主键',
  `brand_id`     BIGINT       NOT NULL COMMENT '品牌id',
  `category_id`  BIGINT       NOT NULL COMMENT '分类id',
  `name`         VARCHAR(128) NOT NULL COMMENT '商品名',
  `subtitle`     VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  `cover_img`    VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `images`       JSON         DEFAULT NULL COMMENT '轮播图(JSON数组)',
  `detail_html`  LONGTEXT     DEFAULT NULL COMMENT '详情富文本',
  `sale_type`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0普通 1食品',
  `pet_type`     TINYINT      NOT NULL DEFAULT 0 COMMENT '适用宠物 0通用 1猫 2狗',
  `is_near_expiry` TINYINT    NOT NULL DEFAULT 0 COMMENT '是否临期 0否 1是',
  `shelf_status` TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  `sales`        INT          NOT NULL DEFAULT 0 COMMENT '销量',
  `create_time`  DATETIME     NOT NULL,
  `update_time`  DATETIME     DEFAULT NULL,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_brand` (`brand_id`),
  KEY `idx_status` (`shelf_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

CREATE TABLE IF NOT EXISTS `tb_sku` (
  `id`             BIGINT       NOT NULL COMMENT '主键',
  `product_id`     BIGINT       NOT NULL COMMENT '商品id',
  `spec_name`      VARCHAR(64)  DEFAULT NULL COMMENT '规格名（如 2kg）',
  `price`          DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '划线价',
  `is_discount`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否折扣价 0否 1是',
  `discount_price` DECIMAL(10,2) DEFAULT NULL COMMENT '折扣价(临期7折落地)',
  `stock`          INT          NOT NULL DEFAULT 0 COMMENT '库存',
  `sales`          INT          NOT NULL DEFAULT 0 COMMENT '销量',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '0停售 1在售',
  `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_time`    DATETIME     NOT NULL,
  `update_time`    DATETIME     DEFAULT NULL,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU';

CREATE TABLE IF NOT EXISTS `tb_sku_batch` (
  `id`             BIGINT       NOT NULL COMMENT '主键',
  `sku_id`         BIGINT       NOT NULL COMMENT 'SKU id',
  `batch_no`       VARCHAR(64)  DEFAULT NULL COMMENT '批次号',
  `produce_date`   DATE         DEFAULT NULL COMMENT '生产日期',
  `expiry_days`    INT          DEFAULT NULL COMMENT '保质期(天)',
  `remain_ratio`   DECIMAL(5,2) DEFAULT NULL COMMENT '剩余保质期占比',
  `is_near_expiry` TINYINT      NOT NULL DEFAULT 0 COMMENT '是否临期 0否 1是',
  `create_time`    DATETIME     NOT NULL,
  `update_time`    DATETIME     DEFAULT NULL,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_sku` (`sku_id`),
  KEY `idx_near` (`is_near_expiry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU批次(食品)';

-- ---------- 购物车域 ----------

CREATE TABLE IF NOT EXISTS `tb_cart` (
  `id`          BIGINT   NOT NULL COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户id',
  `sku_id`      BIGINT   NOT NULL COMMENT 'SKU id',
  `quantity`    INT      NOT NULL DEFAULT 1 COMMENT '数量',
  `selected`    TINYINT  NOT NULL DEFAULT 1 COMMENT '是否勾选 0否 1是',
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车(持久化底账)';

-- ---------- 订单域 ----------

CREATE TABLE IF NOT EXISTS `tb_order` (
  `id`             BIGINT        NOT NULL COMMENT '主键',
  `order_no`       VARCHAR(32)   NOT NULL COMMENT '订单号',
  `user_id`        BIGINT        NOT NULL COMMENT '用户id',
  `receiver`       JSON          NOT NULL COMMENT '收货信息快照',
  `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '0待支付 1待发货 2待收货 3已完成 4已取消 5售后中',
  `goods_amount`   DECIMAL(10,2) NOT NULL COMMENT '商品金额',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `freight`        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费',
  `pay_amount`     DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `pay_status`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
  `remark`         VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
  `pay_time`       DATETIME      DEFAULT NULL,
  `ship_time`      DATETIME      DEFAULT NULL,
  `finish_time`    DATETIME      DEFAULT NULL,
  `cancel_time`    DATETIME      DEFAULT NULL,
  `create_time`    DATETIME      NOT NULL,
  `update_time`    DATETIME      DEFAULT NULL,
  `deleted`        TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE IF NOT EXISTS `tb_order_item` (
  `id`            BIGINT        NOT NULL COMMENT '主键',
  `order_id`      BIGINT        NOT NULL COMMENT '订单id',
  `sku_id`        BIGINT        NOT NULL COMMENT 'SKU id',
  `product_name`  VARCHAR(128)  NOT NULL COMMENT '商品名快照',
  `sku_img`       VARCHAR(255)  DEFAULT NULL COMMENT 'SKU图快照',
  `spec`          VARCHAR(64)   DEFAULT NULL COMMENT '规格快照',
  `price`         DECIMAL(10,2) NOT NULL COMMENT '成交单价快照',
  `quantity`      INT           NOT NULL COMMENT '数量',
  `subtotal`      DECIMAL(10,2) NOT NULL COMMENT '小计',
  `batch_id`      BIGINT        DEFAULT NULL COMMENT '发货批次',
  `produce_date`  DATE          DEFAULT NULL COMMENT '生产日期快照',
  `expiry_date`   DATE          DEFAULT NULL COMMENT '到期日快照',
  `create_time`   DATETIME      NOT NULL,
  `update_time`   DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ---------- 支付域 ----------

CREATE TABLE IF NOT EXISTS `tb_payment` (
  `id`          BIGINT        NOT NULL COMMENT '主键',
  `order_id`    BIGINT        NOT NULL COMMENT '订单id',
  `pay_no`      VARCHAR(32)   NOT NULL COMMENT '支付单号',
  `channel`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0模拟 1微信(预留)',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败',
  `pay_time`    DATETIME      DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME      NOT NULL,
  `update_time` DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

-- ---------- 物流域 ----------

CREATE TABLE IF NOT EXISTS `tb_shipping` (
  `id`           BIGINT      NOT NULL COMMENT '主键',
  `order_id`     BIGINT      NOT NULL COMMENT '订单id',
  `logistics_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
  `tracking_no`  VARCHAR(64) DEFAULT NULL COMMENT '运单号',
  `status`       TINYINT     NOT NULL DEFAULT 0 COMMENT '0已发货 1运输中 2已签收',
  `ship_time`    DATETIME    DEFAULT NULL,
  `receive_time` DATETIME    DEFAULT NULL,
  `create_time`  DATETIME    NOT NULL,
  `update_time`  DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流';

-- ---------- 订阅域 ----------

CREATE TABLE IF NOT EXISTS `tb_subscription` (
  `id`               BIGINT      NOT NULL COMMENT '主键',
  `user_id`          BIGINT      NOT NULL COMMENT '用户id',
  `sku_id`           BIGINT      NOT NULL COMMENT 'SKU id',
  `cycle_days`       INT         NOT NULL DEFAULT 30 COMMENT '周期(天)',
  `status`           TINYINT     NOT NULL DEFAULT 0 COMMENT '0生效 1暂停 2已取消',
  `next_deliver_date` DATE       DEFAULT NULL COMMENT '下次提醒日期',
  `last_notify_time` DATETIME    DEFAULT NULL COMMENT '上次提醒时间',
  `create_time`      DATETIME    NOT NULL,
  `update_time`      DATETIME    DEFAULT NULL,
  `deleted`          TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_next_date` (`next_deliver_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅';

CREATE TABLE IF NOT EXISTS `tb_subscription_notify` (
  `id`              BIGINT   NOT NULL COMMENT '主键',
  `subscription_id` BIGINT   NOT NULL COMMENT '订阅id',
  `user_id`         BIGINT   NOT NULL COMMENT '用户id',
  `notify_time`     DATETIME NOT NULL,
  `create_time`     DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sub` (`subscription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅提醒记录';

-- ---------- 发票域 ----------

CREATE TABLE IF NOT EXISTS `tb_invoice` (
  `id`          BIGINT        NOT NULL COMMENT '主键',
  `user_id`     BIGINT        NOT NULL COMMENT '用户id',
  `order_id`    BIGINT        NOT NULL COMMENT '订单id',
  `type`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0个人 1企业',
  `title`       VARCHAR(128)  NOT NULL COMMENT '抬头',
  `tax_no`      VARCHAR(32)   DEFAULT NULL COMMENT '税号(企业)',
  `email`       VARCHAR(64)   DEFAULT NULL COMMENT '收票邮箱',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '开票金额',
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0已申请 1已开票 2失败',
  `issue_time`  DATETIME      DEFAULT NULL,
  `create_time` DATETIME      NOT NULL,
  `update_time` DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子发票';

-- ---------- 售后域 ----------

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

-- ---------- 评价域 ----------

CREATE TABLE IF NOT EXISTS `tb_review` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `order_id`    BIGINT       NOT NULL COMMENT '订单id',
  `product_id`  BIGINT       NOT NULL COMMENT '商品id',
  `sku_id`      BIGINT       DEFAULT NULL COMMENT 'SKU id',
  `rating`      TINYINT      NOT NULL DEFAULT 5 COMMENT '评分1-5',
  `content`     VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  `images`      JSON         DEFAULT NULL COMMENT '图片(JSON数组)',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0展示 1隐藏',
  `create_time` DATETIME     NOT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

-- ---------- 通知域 ----------

CREATE TABLE IF NOT EXISTS `tb_notification` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `type`        VARCHAR(32)  DEFAULT NULL COMMENT '类型',
  `title`       VARCHAR(128) NOT NULL COMMENT '标题',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '内容',
  `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';

-- ---------- 问答助手域 ----------

CREATE TABLE IF NOT EXISTS `tb_chat_message` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `role`        VARCHAR(16)  NOT NULL COMMENT 'user/assistant',
  `content`     VARCHAR(2000) NOT NULL COMMENT '内容',
  `create_time` DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答消息';

-- ---------- 秒杀域 ----------

CREATE TABLE IF NOT EXISTS `tb_seckill_activity` (
  `id`          BIGINT      NOT NULL COMMENT '主键',
  `name`        VARCHAR(64) NOT NULL COMMENT '场次名',
  `start_time`  DATETIME    NOT NULL COMMENT '开始时间',
  `end_time`    DATETIME    NOT NULL COMMENT '结束时间',
  `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已结束 3关闭(手动)',
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀场次';

CREATE TABLE IF NOT EXISTS `tb_seckill_item` (
  `id`             BIGINT        NOT NULL COMMENT '主键',
  `activity_id`    BIGINT        NOT NULL COMMENT '场次id',
  `sku_id`         BIGINT        NOT NULL COMMENT 'SKU id',
  `seckill_price`  DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
  `seckill_stock`  INT           NOT NULL DEFAULT 0 COMMENT '秒杀库存',
  `seckill_sold`   INT           NOT NULL DEFAULT 0 COMMENT '已抢数量',
  `per_user_limit` INT           NOT NULL DEFAULT 1 COMMENT '每人限购',
  `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `sort`           INT           NOT NULL DEFAULT 0,
  `create_time`    DATETIME      NOT NULL,
  `update_time`    DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品项';

CREATE TABLE IF NOT EXISTS `tb_seckill_record` (
  `id`          BIGINT   NOT NULL COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户id',
  `item_id`     BIGINT   NOT NULL COMMENT '秒杀商品项id',
  `quantity`    INT      NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
  KEY `idx_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀购买记录';

-- ---------- 社区域 ----------

CREATE TABLE IF NOT EXISTS `tb_post` (
  `id`            BIGINT       NOT NULL COMMENT '主键',
  `user_id`       BIGINT       NOT NULL COMMENT '用户id',
  `pet_id`        BIGINT       DEFAULT NULL COMMENT '关联宠物(可空)',
  `content`       VARCHAR(1000) DEFAULT NULL COMMENT '内容',
  `images`        JSON         DEFAULT NULL COMMENT '图片(JSON数组)',
  `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0展示 1隐藏',
  `create_time`   DATETIME     NOT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物日常动态';

CREATE TABLE IF NOT EXISTS `tb_post_like` (
  `id`          BIGINT   NOT NULL COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户id',
  `post_id`     BIGINT   NOT NULL COMMENT '动态id',
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞';

CREATE TABLE IF NOT EXISTS `tb_post_comment` (
  `id`          BIGINT       NOT NULL COMMENT '主键',
  `post_id`     BIGINT       NOT NULL COMMENT '动态id',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
  `create_time` DATETIME     NOT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论';

-- ---------- 收藏域 ----------

CREATE TABLE IF NOT EXISTS `tb_favorite` (
  `id`          BIGINT   NOT NULL COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户id',
  `product_id`  BIGINT   NOT NULL COMMENT '商品id',
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏';

-- ---------- 足迹域 ----------

CREATE TABLE IF NOT EXISTS `tb_footprint` (
  `id`            BIGINT   NOT NULL COMMENT '主键',
  `user_id`       BIGINT   NOT NULL COMMENT '用户id',
  `product_id`    BIGINT   NOT NULL COMMENT '商品id',
  `view_count`    INT      NOT NULL DEFAULT 1 COMMENT '浏览次数',
  `last_view_time` DATETIME NOT NULL COMMENT '最近浏览时间',
  `create_time`   DATETIME NOT NULL,
  `update_time`   DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品浏览足迹';

-- ---------- 后台域 ----------

CREATE TABLE IF NOT EXISTS `tb_admin_user` (
  `id`              BIGINT       NOT NULL COMMENT '主键',
  `username`        VARCHAR(64)  NOT NULL COMMENT '账号',
  `password_hash`   VARCHAR(128) NOT NULL COMMENT '密码',
  `nickname`        VARCHAR(64)  DEFAULT NULL,
  `status`          TINYINT      NOT NULL DEFAULT 1,
  `role_id`         BIGINT       DEFAULT NULL COMMENT '角色id',
  `last_login_time` DATETIME     DEFAULT NULL,
  `create_time`     DATETIME     NOT NULL,
  `update_time`     DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户';

CREATE TABLE IF NOT EXISTS `tb_role` (
  `id`          BIGINT      NOT NULL COMMENT '主键',
  `name`        VARCHAR(64) NOT NULL COMMENT '角色名',
  `code`        VARCHAR(64) NOT NULL COMMENT '角色编码',
  `permission`  JSON        DEFAULT NULL COMMENT '权限(JSON)',
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- ---------- 基础数据 ----------

INSERT INTO `tb_category` (`id`, `parent_id`, `name`, `level`, `sort`, `status`, `create_time`) VALUES
(1, 0, '主粮', 1, 1, 1, NOW()),
(2, 0, '零食', 1, 2, 1, NOW()),
(3, 0, '玩具', 1, 3, 1, NOW()),
(4, 0, '清洁护理', 1, 4, 1, NOW()),
(5, 0, '医疗保健', 1, 5, 1, NOW()),
(6, 0, '智能设备', 1, 6, 1, NOW()),
(7, 0, '服饰', 1, 7, 1, NOW()),
(8, 0, '出行', 1, 8, 1, NOW());

INSERT INTO `tb_brand` (`id`, `name`, `description`, `status`, `sort`, `create_time`) VALUES
(1, '霍曼', '智能宠物用品与清洁护理', 1, 1, NOW()),
(2, '小佩', '宠物智能科技用品', 1, 2, NOW()),
(3, '华元', '宠物用品综合', 1, 3, NOW()),
(4, '麦富迪', '宠物食品专业品牌', 1, 4, NOW());

-- ---------- 好用排行：商品日销量统计 ----------

CREATE TABLE IF NOT EXISTS `tb_product_sales_stat` (
  `id`          BIGINT   NOT NULL COMMENT '主键',
  `product_id`  BIGINT   NOT NULL COMMENT '商品id',
  `stat_date`   DATE     NOT NULL COMMENT '统计日期',
  `qty`         INT      NOT NULL DEFAULT 0 COMMENT '当日销量',
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_date` (`product_id`, `stat_date`),
  KEY `idx_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品日销量统计';

INSERT INTO `tb_product_sales_stat` (`id`, `product_id`, `stat_date`, `qty`, `create_time`)
SELECT
    p.id * 100 + d.n                       AS id,
    p.id                                   AS product_id,
    DATE_SUB(CURDATE(), INTERVAL d.n DAY)  AS stat_date,
    FLOOR(20 + (p.id % 37) * 3 + (d.n * 13 + p.id) % 40) AS qty,
    NOW()
FROM `tb_product` p
JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
) d
WHERE p.deleted = 0 AND p.shelf_status = 1
ON DUPLICATE KEY UPDATE `qty` = VALUES(`qty`), `update_time` = NOW();

-- ---------- 宠物档案：犬猫品种图鉴 ----------

CREATE TABLE IF NOT EXISTS `tb_pet_breed` (
  `id`         BIGINT       NOT NULL COMMENT '主键',
  `type`       TINYINT      NOT NULL COMMENT '1猫 2狗',
  `name`       VARCHAR(64)  NOT NULL COMMENT '品种名',
  `en_name`    VARCHAR(64)  DEFAULT NULL COMMENT '英文名',
  `cover_img`  VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `intro`      VARCHAR(255) DEFAULT NULL COMMENT '一句话简介',
  `description` TEXT        DEFAULT NULL COMMENT '详细介绍',
  `habits`     TEXT         DEFAULT NULL COMMENT '生活习性',
  `appearance` TEXT         DEFAULT NULL COMMENT '外观与颜色',
  `care_tips`  TEXT         DEFAULT NULL COMMENT '饲养建议',
  `size`       VARCHAR(32)  DEFAULT NULL COMMENT '体型',
  `hair`       VARCHAR(32)  DEFAULT NULL COMMENT '毛长',
  `character_trait` VARCHAR(64) DEFAULT NULL COMMENT '性格',
  `origin`     VARCHAR(64)  DEFAULT NULL COMMENT '原产地',
  `life_span`  VARCHAR(32)  DEFAULT NULL COMMENT '寿命',
  `difficulty` TINYINT      NOT NULL DEFAULT 1 COMMENT '饲养难度1-3',
  `sort`       INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  `create_time` DATETIME    NOT NULL,
  `update_time` DATETIME    DEFAULT NULL,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物品种图鉴';

INSERT INTO `tb_pet_breed`
  (`id`, `type`, `name`, `en_name`, `cover_img`, `intro`, `description`, `habits`, `appearance`, `care_tips`,
   `size`, `hair`, `character_trait`, `origin`, `life_span`, `difficulty`, `sort`, `status`, `create_time`) VALUES
(3001, 1, '英国短毛猫', 'British Shorthair', '/breeds/3001.svg', '圆脸厚毛、性情温和的"蓝胖子"，适合新手与家庭饲养。', '', '性格沉稳安静，独立性强但亲人；日常运动量中等，喜欢安静地趴在主人身边。对环境适应力好，较少因搬家换环境而应激。', '体型圆胖结实，四肢粗短；被毛短而浓密、触感如绒；最经典为蓝灰色，另有银渐层、金渐层、乳色、三花等花色；眼睛大而圆，多为铜色或金绿色。', '易发胖，需控制喂食量与热量；短毛也要每周梳毛 2-3 次减少浮毛；注意心脏（心肌病）与牙齿健康，建议定期体检。', '中大型', '短毛', '温顺安静', '英国', '12-17 年', 1, 1, 1, NOW()),
(3002, 1, '美国短毛猫', 'American Shorthair', '/breeds/3002.svg', '健壮活泼的虎斑猫，运动量大、体质好。', '', '精力充沛、爱攀爬跳跃，喜欢互动游戏与追逐玩具；性格独立又友善，能自得其乐。', '肌肉发达、体态匀称；被毛短厚、质地偏硬；以银虎斑最经典，另有棕虎斑、纯白、黑白、三花等；眼睛多为金色或绿色。', '提供猫爬架与逗猫玩具消耗精力；易有肥胖倾向，注意运动与定量喂养；体质较好的品种，定期疫苗驱虫即可。', '中大型', '短毛', '活泼机敏', '美国', '15-20 年', 1, 2, 1, NOW()),
(3003, 1, '布偶猫', 'Ragdoll', '/breeds/3003.svg', '蓝眼睛、性格粘人的"仙女猫"，抱起来软乎乎。', '', '性格极其温顺粘人，被抱起时会放松身体；喜欢跟随主人，适合室内饲养，社交需求高。', '体型大、被毛半长而蓬松；标志性蓝色眼睛；重点色（脸/耳/尾/四肢较深）为主，分双色、手套色、重点色等。', '需每天梳毛防打结；黏人不宜长时间独处；注意心脏（HCM）与肾脏健康，建议定期筛查。', '大型', '长毛', '温顺粘人', '美国', '12-15 年', 2, 3, 1, NOW()),
(3004, 1, '暹罗猫', 'Siamese', '/breeds/3004.svg', '挖煤脸、话多粘人，重点色鼻祖。', '', '聪明好动、极度粘人，喜欢"说话"（叫声响亮）；需要主人大量陪伴与互动，不适合长期独处。', '身型修长纤细、四肢细长；短毛紧贴；脸、耳、尾、四肢呈深色重点色（挖煤脸），眼睛为亮蓝色。', '多陪伴、多互动游戏；注意保暖（短毛怕冷）；留意牙齿与呼吸道问题，定期体检。', '中型', '短毛', '话多粘人', '泰国', '12-20 年', 2, 4, 1, NOW()),
(3005, 1, '缅因猫', 'Maine Coon', '/breeds/3005.svg', '体型最大的家猫，像"温柔的巨人"。', '', '性格温和友善，能与狗、孩子相处；喜欢跟随与互动，也爱玩水。活动空间需求较大。', '体格魁梧、尾巴蓬松似浣熊；有耳尖毛与颈部毛领；毛色丰富（棕虎斑、银、黑、白等）；眼睛大、多为绿色或金色。', '需要较大活动空间与结实的猫爬架；长毛需经常梳理；关注心脏（HCM）与髋关节健康。', '超大型', '长毛', '温和友善', '美国', '10-15 年', 2, 5, 1, NOW()),
(3006, 1, '波斯猫', 'Persian', '/breeds/3006.svg', '长毛贵族，脸扁毛长，气质高雅。', '', '性格安静慵懒，活动量小，喜欢独处休息；对环境变化较敏感，需安静稳定。', '被毛长而华丽、毛量丰厚；面部扁平、鼻梁短；毛色繁多（白、银、金、蓝、黑等）；眼睛大而圆。', '需每天梳毛防打结；扁脸易有泪痕与呼吸道问题，需每日清洁眼周、用浅口食盆；注意饮食与肾脏健康。', '中型', '长毛', '安静慵懒', '伊朗', '12-17 年', 3, 6, 1, NOW()),
(3007, 1, '异国短毛猫（加菲）', 'Exotic Shorthair', '/breeds/3007.svg', '短毛版波斯，圆脸扁嘴、憨态可掬。', '', '性格温顺安静、亲人但不闹腾；活动量不大，喜欢待在主人身边，适合公寓饲养。', '圆脸扁鼻、表情"委屈"；短毛蓬松易打理；毛色与波斯类似（白、金、蓝、红等）；眼睛大而圆。', '短毛但需定期梳理；注意泪痕清洁与呼吸畅通，食盆用浅口；控制体重，定期体检。', '中型', '短毛', '温顺安静', '美国', '12-15 年', 2, 7, 1, NOW()),
(3008, 1, '中华狸花猫', 'Dragon Li', '/breeds/3008.svg', '本土田园猫，虎斑纹、独立强壮、好养活。', '', '独立机敏、捕猎能力强、好奇心重；精力旺盛，喜欢攀爬探索，对陌生环境适应力好。', '体态匀称结实、肌肉利落；被毛短而贴身；典型黑褐虎斑纹（额"王"字、四肢环纹）；眼睛黄绿色。', '体质强健、抗病力较好；保证运动与磨爪需求即可；注意定期驱虫疫苗，控制体重。', '中型', '短毛', '独立机敏', '中国', '15-20 年', 1, 8, 1, NOW()),
(3009, 1, '橘猫', 'Orange Tabby', '/breeds/3009.svg', '"十个橘猫九个胖"，亲和能吃的大暖猫。', '', '性格亲人和善、胆子偏大，食欲旺盛；爱粘人也爱撒娇，与小孩相处友好。', '毛色为橘黄虎斑（深色条纹），底色由浅橘到深橘不等；眼睛多为金色；体型多偏壮实。', '极易发胖，务必控制热量、定量喂食并增加运动；易有口腔与泌尿问题，注意饮水量。', '中大型', '短毛', '亲和贪吃', '各地', '15-20 年', 1, 9, 1, NOW()),
(3010, 1, '斯芬克斯无毛猫', 'Sphynx', '/breeds/3010.svg', '无毛、皮肤皱褶，性格热情像小狗。', '', '性格热情外向、极度粘人，喜欢依偎取暖；活动量大、好奇爱玩，几乎不会"高冷"。', '看似无毛实则皮肤覆有细绒；皮肤多皱褶、体温偏高；毛色体现在皮肤色（白、灰、花斑等）；耳朵大、眼睛大。', '需频繁清洁皮肤与耳道（易出油积垢）；不抗寒，天冷要穿衣保暖；注意心脏与皮肤健康。', '中型', '无毛', '活泼亲人', '加拿大', '9-15 年', 3, 10, 1, NOW()),
(4001, 2, '金毛寻回犬', 'Golden Retriever', '/breeds/4001.svg', '温柔耐心、适合家庭的"暖男犬"。', '', '性格稳重友善、服从性高，喜欢陪伴与户外活动；对孩子和其他宠物都友好，乐于学习取悦主人。', '体型健壮匀称；被毛长而浓密、呈波浪状，色彩由奶油色到金黄色；尾巴蓬松，耳朵自然下垂。', '每天保证 1-2 小时运动；长毛需经常梳理、换毛季更频繁；注意髋关节与皮肤健康。', '大型', '长毛', '温顺友善', '英国', '10-12 年', 2, 1, 1, NOW()),
(4002, 2, '拉布拉多', 'Labrador Retriever', '/breeds/4002.svg', '贪吃爱玩、聪明易训，导盲犬首选。', '', '精力旺盛、爱玩水与衔取，学习能力强、非常听话；喜欢与人互动，需足量运动消耗精力。', '体格结实、胸宽；被毛短而密实、防水；毛色为黑、黄、巧克力三色；尾巴粗壮似"水獭尾"。', '极易发胖，务必控制食量；每天足量运动；注意髋关节与耳道清洁。', '大型', '短毛', '活泼忠诚', '加拿大', '10-12 年', 2, 2, 1, NOW()),
(4003, 2, '柯基', 'Welsh Corgi', '/breeds/4003.svg', '小短腿大屁股，活泼爱叫的小型牧羊犬。', '', '活泼机警、精力充沛、警惕性高爱叫；聪明好学，喜欢参与活动，需要适度运动与训练。', '身长腿短、耳朵直立；被毛中长、双层；常见橙白、黑三色、红白；眼睛圆亮，表情机灵。', '注意脊椎与关节：避免频繁上下楼梯和跳跃；控制体重；提供磨牙与互动玩具。', '中小型', '中长毛', '活泼机警', '英国', '12-15 年', 2, 3, 1, NOW()),
(4004, 2, '泰迪（贵宾犬）', 'Poodle', '/breeds/4004.svg', '卷毛、少掉毛、聪明，城市饲养热门。', '', '智商极高、易训练，粘人爱撒娇；精力好、喜欢互动，独处久了易分离焦虑吠叫。', '被毛卷曲浓密、几乎不掉毛；体型从玩具到标准不等；毛色有棕、白、黑、灰、香槟等。', '需定期修剪造型与梳毛（防打结）；多陪伴、训练缓解焦虑；注意泪痕与牙齿清洁。', '小型', '卷毛', '聪明粘人', '德国', '12-18 年', 2, 4, 1, NOW()),
(4005, 2, '哈士奇', 'Siberian Husky', '/breeds/4005.svg', '"二哈"：精力爆表、爱拆家的雪橇犬。', '', '精力极其旺盛、好奇心强、爱挖爱拆；性格独立、喜群居，运动量与陪伴需求都很高。', '体格结实、四肢有力；被毛中长、双层厚实；毛色有黑白、灰白、红白等；眼睛常见蓝或褐，可有"异瞳"。', '每天大量运动（1-2 小时以上）否则易拆家；耐寒不耐热，注意防暑；换毛季掉毛量大需勤梳。', '中大型', '中长毛', '活泼顽皮', '俄罗斯', '12-14 年', 3, 5, 1, NOW()),
(4006, 2, '萨摩耶', 'Samoyed', '/breeds/4006.svg', '白色微笑天使，毛量惊人。', '', '性格开朗友善、爱笑、喜欢与人亲近；精力充沛、爱户外活动，几乎不具攻击性。', '全身被毛纯白或奶白、蓬松厚实，有"微笑"表情与卷曲尾巴；体型中大型、结实匀称。', '掉毛量极大，需每天梳理；保证运动量；耐寒不耐热，夏季注意防暑降温。', '中大型', '长毛', '友好亲人', '俄罗斯', '12-14 年', 2, 6, 1, NOW()),
(4007, 2, '柴犬', 'Shiba Inu', '/breeds/4007.svg', '表情包担当，独立、爱干净、有点小倔强。', '', '性格独立、领地意识与警戒心强、爱干净；不喜过度亲昵，有自己的小脾气，训练需耐心。', '体型中小、紧凑结实；耳朵直立、尾巴卷曲；毛色有赤（橘棕）、黑、胡麻等，常见"白口罩"；眼睛三角上扬。', '出门务必牵绳（警惕性强易爆冲）；掉毛量中等需定期梳毛；注意关节与皮肤健康。', '中小型', '短毛', '独立机警', '日本', '12-15 年', 2, 7, 1, NOW()),
(4008, 2, '边境牧羊犬', 'Border Collie', '/breeds/4008.svg', '智商第一的工作犬，学习能力惊人。', '', '聪明专注、精力旺盛、学习力极强，喜欢有挑战的任务与奔跑；精力无处消耗易出现行为问题。', '体型中等、匀称灵活；被毛中长、可黑白色或棕色等；眼睛多为深色，表情机敏。', '每天大量运动 + 训练/益智游戏；不适合长时间独处；注意关节与视力健康。', '中型', '中长毛', '聪明专注', '英国', '12-15 年', 3, 8, 1, NOW()),
(4009, 2, '博美', 'Pomeranian', '/breeds/4009.svg', '毛球小狮子，机警爱叫的小型犬。', '', '性格活泼机警、警惕性高爱叫；亲人粘人，喜欢在主人身边，需从小做社会化训练。', '体型小巧、被毛蓬松呈"毛球"状；毛色有橙、棕、白、黑等；耳朵小直立，尾巴蓬松上翘。', '易吠叫，需训练与社会化；注意气管（提拉勿用项圈）与牙齿、泪痕清洁；定期修剪被毛。', '小型', '长毛', '活泼机警', '德国', '12-16 年', 2, 9, 1, NOW()),
(4010, 2, '比熊', 'Bichon Frise', '/breeds/4010.svg', '棉花糖炸毛，性格温和、掉毛少。', '', '性格开朗活泼、温和亲人，爱撒娇也爱玩；掉毛少、体味小，适合公寓饲养。', '被毛纯白、卷曲蓬松如棉花糖；体型小巧、圆润；眼睛与鼻子黑色，表情甜美。', '需定期修剪造型与每日梳毛；注意泪痕清洁（白毛易显）；控制体重、定期洗护。', '小型', '卷毛', '温和活泼', '法国', '14-15 年', 2, 10, 1, NOW());

CREATE TABLE IF NOT EXISTS `tb_breed_recommend` (
  `id`         BIGINT  NOT NULL COMMENT '主键',
  `breed_id`   BIGINT  NOT NULL COMMENT '品种id',
  `product_id` BIGINT  NOT NULL COMMENT '商品id',
  `type`       TINYINT NOT NULL DEFAULT 1 COMMENT '1主粮 2其他',
  `sort`       INT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_breed` (`breed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品种推荐商品';

INSERT INTO `tb_breed_recommend` (`id`, `breed_id`, `product_id`, `type`, `sort`) VALUES
(30011, 3001, 1001, 1, 1),
(30012, 3001, 1101, 1, 2),
(30013, 3001, 1102, 2, 3),
(30021, 3002, 1001, 1, 1),
(30022, 3002, 1101, 1, 2),
(30023, 3002, 1004, 2, 3),
(30031, 3003, 1001, 1, 1),
(30032, 3003, 1101, 1, 2),
(30033, 3003, 1108, 2, 3),
(30041, 3004, 1001, 1, 1),
(30042, 3004, 1101, 1, 2),
(30043, 3004, 1103, 2, 3),
(30051, 3005, 1001, 1, 1),
(30052, 3005, 1101, 1, 2),
(30053, 3005, 1203, 2, 3),
(30061, 3006, 1001, 1, 1),
(30062, 3006, 1101, 1, 2),
(30063, 3006, 1109, 2, 3),
(30071, 3007, 1001, 1, 1),
(30072, 3007, 1101, 1, 2),
(30073, 3007, 1107, 2, 3),
(30081, 3008, 1001, 1, 1),
(30082, 3008, 1101, 1, 2),
(30083, 3008, 1004, 2, 3),
(30091, 3009, 1001, 1, 1),
(30092, 3009, 1101, 1, 2),
(30093, 3009, 1102, 2, 3),
(30101, 3010, 1001, 1, 1),
(30102, 3010, 1101, 1, 2),
(30103, 3010, 1201, 2, 3),
(40011, 4001, 1002, 1, 1),
(40012, 4001, 1104, 1, 2),
(40013, 4001, 1111, 2, 3),
(40021, 4002, 1002, 1, 1),
(40022, 4002, 1104, 1, 2),
(40023, 4002, 1112, 2, 3),
(40031, 4003, 1002, 1, 1),
(40032, 4003, 1104, 1, 2),
(40033, 4003, 1304, 2, 3),
(40041, 4004, 1002, 1, 1),
(40042, 4004, 1104, 1, 2),
(40043, 4004, 1202, 2, 3),
(40051, 4005, 1002, 1, 1),
(40052, 4005, 1104, 1, 2),
(40053, 4005, 1111, 2, 3),
(40061, 4006, 1002, 1, 1),
(40062, 4006, 1104, 1, 2),
(40063, 4006, 1302, 2, 3),
(40071, 4007, 1002, 1, 1),
(40072, 4007, 1104, 1, 2),
(40073, 4007, 1308, 2, 3),
(40081, 4008, 1002, 1, 1),
(40082, 4008, 1104, 1, 2),
(40083, 4008, 1304, 2, 3),
(40091, 4009, 1002, 1, 1),
(40092, 4009, 1104, 1, 2),
(40093, 4009, 1105, 2, 3),
(40101, 4010, 1002, 1, 1),
(40102, 4010, 1104, 1, 2),
(40103, 4010, 1204, 2, 3);

-- ---------- 站内通知 ----------

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
