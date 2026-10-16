-- 秒杀演示数据修正：名称改为「秒杀特惠」，补齐到 5 个品类
USE `mall`;

UPDATE tb_seckill_activity SET name = '秒杀特惠';

SET @rn := (SELECT COALESCE(MAX(id), 0) FROM tb_seckill_item);

INSERT INTO tb_seckill_item (id, activity_id, sku_id, seckill_price, seckill_stock, seckill_sold, per_user_limit, status, sort, create_time, update_time)
SELECT
  @rn := @rn + 1,
  a.id,
  t.sku_id,
  t.price,
  5,
  0,
  1,
  1,
  t.sort,
  NOW(),
  NOW()
FROM tb_seckill_activity a
JOIN (
  SELECT 2102 AS sku_id, 15.90 AS price, 2 AS sort
  UNION ALL SELECT 2004, 29.90, 3
  UNION ALL SELECT 2203, 59.90, 4
  UNION ALL SELECT 2005, 19.90, 5
) t
  ON a.id = (SELECT id FROM tb_seckill_activity ORDER BY start_time LIMIT 1)
 WHERE t.sku_id NOT IN (SELECT sku_id FROM tb_seckill_item WHERE activity_id = a.id);