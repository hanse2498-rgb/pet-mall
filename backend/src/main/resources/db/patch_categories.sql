-- 新增分类：服饰 / 出行 + 演示商品（补丁）
USE `mall`;

INSERT IGNORE INTO tb_category (id, parent_id, name, level, sort, status, create_time) VALUES
(7, 0, '服饰', 1, 7, 1, NOW()),
(8, 0, '出行', 1, 8, 1, NOW());

INSERT INTO tb_product (id, brand_id, category_id, name, subtitle, cover_img, images, detail_html, sale_type, pet_type, is_near_expiry, shelf_status, sales, create_time, update_time, deleted) VALUES
(1201, 2, 7, '宠物连帽卫衣 猫狗通用', '秋冬加绒 时尚保暖', 'https://picsum.photos/seed/p1201/600/600', '["https://picsum.photos/seed/p1201a/800/800","https://picsum.photos/seed/p1201b/800/800"]', '<h3>加绒卫衣</h3><p>柔软亲肤，猫狗通用。</p>', 0, 0, 0, 1, 210, NOW(), NOW(), 0),
(1202, 3, 7, '宠物雨衣 四脚款', '加厚防水 反光条', 'https://picsum.photos/seed/p1202/600/600', '["https://picsum.photos/seed/p1202a/800/800"]', '<h3>防水雨衣</h3><p>雨天遛宠不湿身。</p>', 0, 0, 0, 1, 180, NOW(), NOW(), 0),
(1203, 1, 8, '宠物外出双肩包 透气猫包', '可登机 侧窗透气', 'https://picsum.photos/seed/p1203/600/600', '["https://picsum.photos/seed/p1203a/800/800","https://picsum.photos/seed/p1203b/800/800"]', '<h3>透明外出包</h3><p>大侧窗透气，带猫咪看世界。</p>', 0, 1, 0, 1, 320, NOW(), NOW(), 0),
(1204, 2, 8, '车载宠物坐垫 防滑垫', '易清洗 防抓耐脏', 'https://picsum.photos/seed/p1204/600/600', '["https://picsum.photos/seed/p1204a/800/800"]', '<h3>车载坐垫</h3><p>防滑隔脏，出行更安心。</p>', 0, 2, 0, 1, 150, NOW(), NOW(), 0);

INSERT INTO tb_sku (id, product_id, spec_name, price, original_price, is_discount, discount_price, stock, sales, status, version, create_time, update_time, deleted) VALUES
(2201, 1201, 'M号', 59.90, 79.90, 0, NULL, 300, 210, 1, 0, NOW(), NOW(), 0),
(2202, 1202, '四脚款', 45.90, 59.90, 0, NULL, 400, 180, 1, 0, NOW(), NOW(), 0),
(2203, 1203, '标准款', 89.90, 109.90, 0, NULL, 280, 320, 1, 0, NOW(), NOW(), 0),
(2204, 1204, '中号', 69.90, 89.90, 0, NULL, 240, 150, 1, 0, NOW(), NOW(), 0);