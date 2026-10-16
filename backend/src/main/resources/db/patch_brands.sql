-- 品牌改为：霍曼 / 小佩 / 华元 / 麦富迪（去掉示例品牌A/B/C）
SET NAMES utf8mb4;

-- 1) 重命名现有品牌
UPDATE `tb_brand` SET `name` = '霍曼', `description` = '智能宠物用品与清洁护理', `update_time` = NOW() WHERE `id` = 1;
UPDATE `tb_brand` SET `name` = '小佩', `description` = '宠物智能科技用品', `update_time` = NOW() WHERE `id` = 2;
UPDATE `tb_brand` SET `name` = '华元', `description` = '宠物用品综合', `update_time` = NOW() WHERE `id` = 3;

-- 2) 新增品牌：麦富迪（宠物食品）
INSERT INTO `tb_brand` (`id`, `name`, `description`, `status`, `sort`, `create_time`)
SELECT 4, '麦富迪', '宠物食品专业品牌', 1, 4, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `tb_brand` WHERE `id` = 4);

-- 3) 商品按 id 轮转均分到 4 个品牌，保证每个品牌都有货
UPDATE `tb_product` p
JOIN (
  SELECT id, (ROW_NUMBER() OVER (ORDER BY id) - 1) % 4 + 1 AS nb
  FROM `tb_product`
  WHERE deleted = 0
) t ON t.id = p.id
SET p.brand_id = t.nb, p.update_time = NOW();