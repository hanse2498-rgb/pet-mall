-- ============================================================
-- 好用排行：商品日销量统计 + 近 7 天演示数据
-- ============================================================

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

-- 造最近 7 天演示销量（每个在售商品每天一条，qty 由商品 id 派生，保证同分类内有明显排位差异）
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
