package com.mall.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.module.product.entity.Sku;
import org.apache.ibatis.annotations.Param;

public interface SkuMapper extends BaseMapper<Sku> {

    /** 扣减库存（stock 足够才成功），成功返回1 */
    int deductStock(@Param("skuId") Long skuId, @Param("qty") Integer qty);

    /** 回补库存 */
    int increaseStock(@Param("skuId") Long skuId, @Param("qty") Integer qty);
}