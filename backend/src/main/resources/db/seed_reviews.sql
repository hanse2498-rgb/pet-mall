-- 演示评价数据（display only，order_id=0 无关联订单）
USE `mall`;

INSERT INTO tb_review (id, user_id, order_id, product_id, sku_id, rating, content, images, status, create_time, update_time) VALUES
(1, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), 0, 1001, 2001, 5, '猫咪特别爱吃，颗粒大小合适，无谷配方很安心，回购！', '["https://picsum.photos/seed/rv1/400/400","https://picsum.photos/seed/rv1b/400/400"]', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(2, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), 0, 1001, 2002, 5, '大包装很划算，拆封后味道新鲜，毛孩子干饭速度超快，点赞。', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(3, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), 0, 1001, 2001, 4, '整体不错，就是颗粒偏小，幼猫应该更合适。会回购。', NULL, 0, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW()),
(4, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), 0, 1001, 2001, 3, '适口性一般，家里猫咪挑食，只吃了一点点，希望改进。', NULL, 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW()),
(5, (SELECT id FROM tb_user WHERE phone='13700002222' LIMIT 1), 0, 1102, 2102, 5, '猫条很香，训练奖励神器，猫咪闻见就跑过来。', '["https://picsum.photos/seed/rv5/400/400"]', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(6, (SELECT id FROM tb_user WHERE phone='13600001111' LIMIT 1), 0, 1102, 2102, 4, '性价比可以，一盒12支刚好一周分量，就是味道有点重。', NULL, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(7, (SELECT id FROM tb_user WHERE phone='13500006666' LIMIT 1), 0, 1003, 2004, 5, '冻干很完整，复水后肉感十足，狗狗吃不腻。', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());