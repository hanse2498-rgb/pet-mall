-- 宠物社区：建表 + 演示动态
USE `mall`;

CREATE TABLE IF NOT EXISTS `tb_post` (
  `id`            BIGINT       NOT NULL,
  `user_id`       BIGINT       NOT NULL,
  `pet_id`        BIGINT       DEFAULT NULL,
  `content`       VARCHAR(1000) DEFAULT NULL,
  `images`        JSON         DEFAULT NULL,
  `like_count`    INT          NOT NULL DEFAULT 0,
  `comment_count` INT          NOT NULL DEFAULT 0,
  `view_count`    INT          NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 0,
  `create_time`   DATETIME     NOT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物日常动态';

CREATE TABLE IF NOT EXISTS `tb_post_like` (
  `id`          BIGINT   NOT NULL,
  `user_id`     BIGINT   NOT NULL,
  `post_id`     BIGINT   NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞';

CREATE TABLE IF NOT EXISTS `tb_post_comment` (
  `id`          BIGINT       NOT NULL,
  `post_id`     BIGINT       NOT NULL,
  `user_id`     BIGINT       NOT NULL,
  `content`     VARCHAR(500) NOT NULL,
  `create_time` DATETIME     NOT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论';

INSERT INTO tb_post (id, user_id, pet_id, content, images, like_count, comment_count, view_count, status, create_time, update_time, deleted) VALUES
(1, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), NULL, '今天带我家猫去楼下晒太阳，它居然主动翻肚皮啦！果然春天到了～', '["https://picsum.photos/seed/pc1a/400/400","https://picsum.photos/seed/pc1b/400/400","https://picsum.photos/seed/pc1c/400/400"]', 12, 3, 230, 0, NOW(), NOW(), 0),
(2, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), NULL, '新买的漏食球到货，狗子研究半天终于吃到了，聪明鬼！', '["https://picsum.photos/seed/pc2a/400/400"]', 8, 2, 150, 0, NOW(), NOW(), 0),
(3, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), NULL, '加班到家，发现猫躺在键盘上不让工作……这算工伤吗？', '["https://picsum.photos/seed/pc3a/400/400","https://picsum.photos/seed/pc3b/400/400"]', 25, 5, 410, 0, NOW(), NOW(), 0),
(4, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), NULL, '第一次洗澡，全程骂骂咧咧但又不敢跑，哈哈哈', '["https://picsum.photos/seed/pc4a/400/400"]', 19, 4, 330, 0, NOW(), NOW(), 0);

INSERT INTO tb_post_comment (id, post_id, user_id, content, create_time, update_time) VALUES
(1, 1, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '翻肚皮就是信任你，太幸福了吧', NOW(), NOW()),
(2, 1, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), '哈哈哈哈春天来了猫也要约会', NOW(), NOW()),
(3, 1, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '多拍点照片呀', NOW(), NOW()),
(4, 2, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), '我家狗子 3 分钟就拆穿了……', NOW(), NOW()),
(5, 2, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), '聪明狗狗！', NOW(), NOW()),
(6, 3, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '这是怕你孤单呀', NOW(), NOW()),
(7, 3, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), '工伤没跑了，找老板报销猫粮', NOW(), NOW()),
(8, 3, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '哈哈哈专属暖手宝', NOW(), NOW()),
(9, 3, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), '笑死，猫主子永远是对的', NOW(), NOW()),
(10, 3, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), '惯着吧，没辙', NOW(), NOW()),
(11, 4, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '哈哈哈表情说明了一切', NOW(), NOW()),
(12, 4, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), '可爱炸了，多来点', NOW(), NOW()),
(13, 4, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), '洗澡就是一场世界大战', NOW(), NOW()),
(14, 4, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), '哭唧唧还是乖乖洗完了', NOW(), NOW());