-- ============================================================
-- 演示数据（可选）  mysql --default-character-set=utf8mb4 < seed_demo.sql
-- 包含 pet_type(0通用 1猫 2狗) 字段
-- ============================================================
USE `mall`;

INSERT INTO tb_product (id, brand_id, category_id, name, subtitle, cover_img, images, detail_html, sale_type, pet_type, is_near_expiry, shelf_status, sales, create_time, update_time, deleted) VALUES
(1001, 1, 1, '全价鸡肉配方猫粮 2kg',  '高蛋白 呵护毛发 肠胃友好', 'https://picsum.photos/seed/p1001/600/600', '["https://picsum.photos/seed/p1001a/800/800","https://picsum.photos/seed/p1001b/800/800"]', '<h3>鲜鸡肉配方</h3><p>无谷配方，添加益生菌与鱼油，适口性好。</p>', 1, 1, 0, 1, 326, NOW(), NOW(), 0),
(1002, 2, 1, '深海鱼成犬粮 10kg', '美毛亮毛 关节呵护', 'https://picsum.photos/seed/p1002/600/600', '["https://picsum.photos/seed/p1002a/800/800"]', '<h3>深海鱼油</h3><p>Omega-3 美毛，添加软骨素。</p>', 1, 2, 0, 1, 512, NOW(), NOW(), 0),
(1003, 3, 2, '冻干鸡肉零食 100g', '纯肉冻干 无添加', 'https://picsum.photos/seed/p1003/600/600', '["https://picsum.photos/seed/p1003a/800/800","https://picsum.photos/seed/p1003b/800/800"]', '<h3>100% 冻干鸡胸肉</h3><p>训练奖励首选。</p>', 1, 1, 0, 1, 1088, NOW(), NOW(), 0),
(1004, 1, 3, '瓦楞纸猫抓板', '加厚耐磨 防飞屑', 'https://picsum.photos/seed/p1004/600/600', '["https://picsum.photos/seed/p1004a/800/800"]', '<h3>加厚瓦楞纸</h3><p>可替换内芯，防止家具抓挠。</p>', 0, 1, 0, 1, 742, NOW(), NOW(), 0),
(1005, 2, 4, '宠物低敏沐浴露 500ml', '草本配方 温和不刺激', 'https://picsum.photos/seed/p1005/600/600', '["https://picsum.photos/seed/p1005a/800/800"]', '<h3>低敏配方</h3><p>适合敏感肌宠物。</p>', 0, 0, 0, 1, 233, NOW(), NOW(), 0),
(1006, 3, 5, '关节养护营养膏', '氨糖软骨素 加钙', 'https://picsum.photos/seed/p1006/600/600', '["https://picsum.photos/seed/p1006a/800/800"]', '<h3>氨糖+软骨素</h3><p>中老年宠物关节保养。</p>', 0, 0, 0, 1, 168, NOW(), NOW(), 0),
(1007, 1, 6, '智能定时喂食器 3L', '手机App 定时定量 断粮提醒', 'https://picsum.photos/seed/p1007/600/600', '["https://picsum.photos/seed/p1007a/800/800","https://picsum.photos/seed/p1007b/800/800"]', '<h3>独居养宠神器</h3><p>可远程定时出粮，出差无忧。</p>', 0, 0, 0, 1, 429, NOW(), NOW(), 0),
(1008, 1, 2, '鸡肉冻干零食 100g（临期）', '临期特价 7折 到手不亏', 'https://picsum.photos/seed/p1008/600/600', '["https://picsum.photos/seed/p1008a/800/800"]', '<h3>临期特惠</h3><p>剩余保质期约1/3，7折出售，建议尽快食用。</p>', 1, 0, 1, 1, 96, NOW(), NOW(), 0),
-- 猫-食品
(1101, 2, 2, '三文鱼猫主食罐头 170g', '整罐鲜肉 补水神器', 'https://picsum.photos/seed/p1101/600/600', '["https://picsum.photos/seed/p1101a/800/800"]', '<h3>三文鱼配方</h3><p>高水分主食罐，帮助不爱喝水的猫补水。</p>', 1, 1, 0, 1, 655, NOW(), NOW(), 0),
(1102, 3, 2, '猫条零食 12支装', '鲜肉猫条 撕开即食', 'https://picsum.photos/seed/p1102/600/600', '["https://picsum.photos/seed/p1102a/800/800"]', '<h3>猫条</h3><p>鸡肉三文鱼口味，互动喂食好帮手。</p>', 1, 1, 0, 1, 2310, NOW(), NOW(), 0),
(1103, 1, 2, '冻干鹌鹑零食 40g', '整只冻干 磨牙补钙', 'https://picsum.photos/seed/p1103/600/600', '["https://picsum.photos/seed/p1103a/800/800"]', '<h3>整鹌鹑冻干</h3><p>天然补钙，猫咪爱不释口。</p>', 1, 1, 0, 1, 301, NOW(), NOW(), 0),
-- 狗-食品
(1104, 1, 1, '鸡肉牛油果成犬粮 2kg', '美毛护肤 适口性好', 'https://picsum.photos/seed/p1104/600/600', '["https://picsum.photos/seed/p1104a/800/800"]', '<h3>鸡肉+牛油果</h3><p>亮毛护肤配方。</p>', 1, 2, 0, 1, 288, NOW(), NOW(), 0),
(1105, 2, 2, '狗咬胶磨牙棒 大号', '洁齿磨牙 耐咬', 'https://picsum.photos/seed/p1105/600/600', '["https://picsum.photos/seed/p1105a/800/800"]', '<h3>磨牙洁齿</h3><p>耐咬配方，去口臭。</p>', 1, 2, 0, 1, 434, NOW(), NOW(), 0),
(1106, 3, 2, '牛肉罐头 370g', '大块牛肉 肉香浓郁', 'https://picsum.photos/seed/p1106/600/600', '["https://picsum.photos/seed/p1106a/800/800"]', '<h3>牛肉罐头</h3><p>拌粮伴侣，适口性极佳。</p>', 1, 2, 0, 1, 521, NOW(), NOW(), 0),
-- 猫-用品
(1107, 2, 4, '豆腐猫砂 6L', '原味低尘 快速结团', 'https://picsum.photos/seed/p1107/600/600', '["https://picsum.photos/seed/p1107a/800/800"]', '<h3>豆腐砂</h3><p>可冲厕所，低粉尘。</p>', 0, 1, 0, 1, 1820, NOW(), NOW(), 0),
(1108, 3, 3, '逗猫棒套装 三件', '羽毛铃铛 猫咪最爱', 'https://picsum.photos/seed/p1108/600/600', '["https://picsum.photos/seed/p1108a/800/800"]', '<h3>逗猫玩具</h3><p>增进互动，消耗体力。</p>', 0, 1, 0, 1, 977, NOW(), NOW(), 0),
(1109, 1, 5, '猫粮碗防滑增高', '不锈钢 防滑底座', 'https://picsum.photos/seed/p1109/600/600', '["https://picsum.photos/seed/p1109a/800/800"]', '<h3>护颈食盆</h3><p>抬高设计保护颈椎。</p>', 0, 1, 0, 1, 366, NOW(), NOW(), 0),
-- 狗-用品
(1110, 3, 3, '漏食玩具球', '慢食漏食 缓解焦虑', 'https://picsum.photos/seed/p1110/600/600', '["https://picsum.photos/seed/p1110a/800/800"]', '<h3>漏食球</h3><p>藏粮解闷，防止拆家。</p>', 0, 2, 0, 1, 610, NOW(), NOW(), 0),
(1111, 1, 3, '伸缩牵引绳 3m', '升级防爆冲 夜光', 'https://picsum.photos/seed/p1111/600/600', '["https://picsum.photos/seed/p1111a/800/800"]', '<h3>伸缩牵引</h3><p>夜光反光更安全。</p>', 0, 2, 0, 1, 892, NOW(), NOW(), 0),
(1112, 2, 3, '狗狗窝柔软坐垫', '可拆洗 冬暖夏凉', 'https://picsum.photos/seed/p1112/600/600', '["https://picsum.photos/seed/p1112a/800/800"]', '<h3>舒适窝垫</h3><p>四季通用，可拆洗。</p>', 0, 2, 0, 1, 754, NOW(), NOW(), 0),
-- 服饰
(1201, 2, 7, '宠物连帽卫衣 猫狗通用', '秋冬加绒 时尚保暖', 'https://picsum.photos/seed/p1201/600/600', '["https://picsum.photos/seed/p1201a/800/800","https://picsum.photos/seed/p1201b/800/800"]', '<h3>加绒卫衣</h3><p>柔软亲肤，猫狗通用，外出保暖又好看。</p>', 0, 0, 0, 1, 210, NOW(), NOW(), 0),
(1202, 3, 7, '宠物雨衣 四脚款', '加厚防水 反光条', 'https://picsum.photos/seed/p1202/600/600', '["https://picsum.photos/seed/p1202a/800/800"]', '<h3>防水雨衣</h3><p>四脚包裹，带反光条，雨天遛宠不湿身。</p>', 0, 0, 0, 1, 180, NOW(), NOW(), 0),
-- 出行
(1203, 1, 8, '宠物外出双肩包 透气猫包', '可登机 侧窗透气', 'https://picsum.photos/seed/p1203/600/600', '["https://picsum.photos/seed/p1203a/800/800","https://picsum.photos/seed/p1203b/800/800"]', '<h3>透明外出包</h3><p>大侧窗透气，背带减压，带猫咪看世界。</p>', 0, 1, 0, 1, 320, NOW(), NOW(), 0),
(1204, 2, 8, '车载宠物坐垫 防滑垫', '易清洗 防抓耐脏', 'https://picsum.photos/seed/p1204/600/600', '["https://picsum.photos/seed/p1204a/800/800"]', '<h3>车载坐垫</h3><p>防滑隔脏，带宠物自驾更安心。</p>', 0, 2, 0, 1, 150, NOW(), NOW(), 0);

-- SKU
INSERT INTO tb_sku (id, product_id, spec_name, price, original_price, is_discount, discount_price, stock, sales, status, version, create_time, update_time, deleted) VALUES
(2001, 1001, '2kg', 129.00, 159.00, 0, NULL, 500, 320, 1, 0, NOW(), NOW(), 0),
(2002, 1001, '5kg', 269.00, 329.00, 0, NULL, 300, 400, 1, 0, NOW(), NOW(), 0),
(2003, 1002, '10kg', 399.00, 459.00, 0, NULL, 120, 500, 1, 0, NOW(), NOW(), 0),
(2004, 1003, '100g', 39.90, 49.90, 0, NULL, 800, 1000, 1, 0, NOW(), NOW(), 0),
(2005, 1004, '标准款', 29.90, 39.90, 0, NULL, 600, 700, 1, 0, NOW(), NOW(), 0),
(2006, 1005, '500ml', 59.00, 69.00, 0, NULL, 400, 200, 1, 0, NOW(), NOW(), 0),
(2007, 1006, '120g', 89.00, 99.00, 0, NULL, 300, 150, 1, 0, NOW(), NOW(), 0),
(2008, 1007, '3L 单机版', 349.00, 399.00, 0, NULL, 200, 400, 1, 0, NOW(), NOW(), 0),
(2009, 1007, '3L + 摄像头套餐', 469.00, 529.00, 0, NULL, 100, 300, 1, 0, NOW(), NOW(), 0),
(2010, 1008, '100g', 27.93, 39.90, 1, 27.93, 50, 90, 1, 0, NOW(), NOW(), 0),
(2101, 1101, '170g×1罐', 15.90, 19.90, 0, NULL, 900, 600, 1, 0, NOW(), NOW(), 0),
(2102, 1102, '12支装', 25.90, 29.90, 0, NULL, 1500, 2200, 1, 0, NOW(), NOW(), 0),
(2103, 1103, '40g', 35.90, 42.90, 0, NULL, 400, 280, 1, 0, NOW(), NOW(), 0),
(2104, 1104, '2kg', 99.00, 129.00, 0, NULL, 350, 270, 1, 0, NOW(), NOW(), 0),
(2105, 1105, '大号×3', 28.90, 36.90, 0, NULL, 600, 410, 1, 0, NOW(), NOW(), 0),
(2106, 1106, '370g', 19.90, 24.90, 0, NULL, 700, 500, 1, 0, NOW(), NOW(), 0),
(2107, 1107, '6L', 39.90, 49.90, 0, NULL, 1000, 1800, 1, 0, NOW(), NOW(), 0),
(2108, 1108, '三件套', 22.90, 28.90, 0, NULL, 800, 950, 1, 0, NOW(), NOW(), 0),
(2109, 1109, '单碗', 32.90, 39.90, 0, NULL, 500, 340, 1, 0, NOW(), NOW(), 0),
(2110, 1110, '中号', 35.90, 42.90, 0, NULL, 450, 580, 1, 0, NOW(), NOW(), 0),
(2111, 1111, '3m', 49.90, 59.90, 0, NULL, 550, 870, 1, 0, NOW(), NOW(), 0),
(2112, 1112, '中号', 69.00, 79.00, 0, NULL, 300, 730, 1, 0, NOW(), NOW(), 0),
(2201, 1201, 'M号', 59.90, 79.90, 0, NULL, 300, 210, 1, 0, NOW(), NOW(), 0),
(2202, 1202, '四脚款', 45.90, 59.90, 0, NULL, 400, 180, 1, 0, NOW(), NOW(), 0),
(2203, 1203, '标准款', 89.90, 109.90, 0, NULL, 280, 320, 1, 0, NOW(), NOW(), 0),
(2204, 1204, '中号', 69.90, 89.90, 0, NULL, 240, 150, 1, 0, NOW(), NOW(), 0);

-- 食品批次
INSERT INTO tb_sku_batch (id, sku_id, batch_no, produce_date, expiry_days, remain_ratio, is_near_expiry, create_time, update_time, deleted) VALUES
(3001, 2001, 'B20261001', '2026-10-01', 540, 0.98, 0, NOW(), NOW(), 0),
(3002, 2002, 'B20260815', '2026-08-15', 540, 0.88, 0, NOW(), NOW(), 0),
(3003, 2003, 'B20260901', '2026-09-01', 720, 0.96, 0, NOW(), NOW(), 0),
(3004, 2004, 'B20261005', '2026-10-05', 365, 0.99, 0, NOW(), NOW(), 0),
(3005, 2010, 'B20261008', '2026-04-08', 365, 0.33, 1, NOW(), NOW(), 0),
(3101, 2101, 'B20261002', '2026-10-02', 180, 0.97, 0, NOW(), NOW(), 0),
(3102, 2102, 'B20261003', '2026-10-03', 270, 0.98, 0, NOW(), NOW(), 0),
(3103, 2103, 'B20260920', '2026-09-20', 365, 0.95, 0, NOW(), NOW(), 0),
(3104, 2104, 'B20261004', '2026-10-04', 540, 0.99, 0, NOW(), NOW(), 0),
(3105, 2105, 'B20261006', '2026-10-06', 365, 0.99, 0, NOW(), NOW(), 0),
(3106, 2106, 'B20261007', '2026-10-07', 270, 0.99, 0, NOW(), NOW(), 0);

-- 补充演示商品（凑够分页，每页30）
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