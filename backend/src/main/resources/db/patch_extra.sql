-- 补充演示商品（凑够分页演示）
USE `mall`;

INSERT INTO tb_product (id, brand_id, category_id, name, subtitle, cover_img, images, detail_html, sale_type, pet_type, is_near_expiry, shelf_status, sales, create_time, update_time, deleted) VALUES
(1301, 3, 7, '宠物芭蕾风连衣裙', '甜美纱裙 好看好穿', 'https://picsum.photos/seed/p1301/600/600', '["https://picsum.photos/seed/p1301a/800/800","https://picsum.photos/seed/p1301b/800/800"]', '<h3>芭蕾裙</h3><p>透气纱裙，拍照上镜。</p>', 0, 1, 0, 1, 96, NOW(), NOW(), 0),
(1302, 1, 7, '狗狗冲锋衣 防泼水', '防风防水 反光条', 'https://picsum.photos/seed/p1302/600/600', '["https://picsum.photos/seed/p1302a/800/800"]', '<h3>冲锋衣</h3><p>户外遛狗防风保暖。</p>', 0, 2, 0, 1, 143, NOW(), NOW(), 0),
(1303, 2, 8, '宠物推车 可折叠', '轻便折叠 一键收纳', 'https://picsum.photos/seed/p1303/600/600', '["https://picsum.photos/seed/p1303a/800/800"]', '<h3>宠物推车</h3><p>带毛孩逛公园省力神器。</p>', 0, 0, 0, 1, 88, NOW(), NOW(), 0),
(1304, 1, 3, '宠物飞盘套装', '耐咬回旋 玩耍互动', 'https://picsum.photos/seed/p1304/600/600', '["https://picsum.photos/seed/p1304a/800/800"]', '<h3>飞盘</h3><p>耐咬材质，狗狗最爱。</p>', 0, 2, 0, 1, 210, NOW(), NOW(), 0),
(1305, 3, 4, '宠物指甲剪套装', '防剪到血线 带锉刀', 'https://picsum.photos/seed/p1305/600/600', '["https://picsum.photos/seed/p1305a/800/800"]', '<h3>指甲剪</h3><p>家用护理必备。</p>', 0, 0, 0, 1, 176, NOW(), NOW(), 0),
(1306, 2, 5, '体内外驱虫滴剂 3支', '广谱驱虫 温和有效', 'https://picsum.photos/seed/p1306/600/600', '["https://picsum.photos/seed/p1306a/800/800"]', '<h3>驱虫滴剂</h3><p>每月一次，驱杀体内外寄生虫。</p>', 0, 0, 0, 1, 265, NOW(), NOW(), 0),
(1307, 3, 6, '智能宠物饮水机 2L', '活水循环 静音过滤', 'https://picsum.photos/seed/p1307/600/600', '["https://picsum.photos/seed/p1307a/800/800"]', '<h3>活水机</h3><p>过滤循环，猫咪更爱喝水。</p>', 0, 1, 0, 1, 320, NOW(), NOW(), 0),
(1308, 1, 3, '藏食嗅闻垫', '解闷防拆家 益智玩具', 'https://picsum.photos/seed/p1308/600/600', '["https://picsum.photos/seed/p1308a/800/800"]', '<h3>嗅闻垫</h3><p>藏零食让毛孩动脑。</p>', 0, 0, 0, 1, 132, NOW(), NOW(), 0);

INSERT INTO tb_sku (id, product_id, spec_name, price, original_price, is_discount, discount_price, stock, sales, status, version, create_time, update_time, deleted) VALUES
(2301, 1301, 'S号', 69.90, 89.90, 0, NULL, 200, 96, 1, 0, NOW(), NOW(), 0),
(2302, 1302, 'M号', 79.90, 99.90, 0, NULL, 180, 143, 1, 0, NOW(), NOW(), 0),
(2303, 1303, '标准款', 199.00, 259.00, 0, NULL, 60, 88, 1, 0, NOW(), NOW(), 0),
(2304, 1304, '两只装', 29.90, 39.90, 0, NULL, 320, 210, 1, 0, NOW(), NOW(), 0),
(2305, 1305, '套装', 39.90, 49.90, 0, NULL, 280, 176, 1, 0, NOW(), NOW(), 0),
(2306, 1306, '3支装', 89.00, 109.00, 0, NULL, 400, 265, 1, 0, NOW(), NOW(), 0),
(2307, 1307, '2L', 129.00, 159.00, 0, NULL, 200, 320, 1, 0, NOW(), NOW(), 0),
(2308, 1308, '中号', 35.90, 45.90, 0, NULL, 350, 132, 1, 0, NOW(), NOW(), 0);