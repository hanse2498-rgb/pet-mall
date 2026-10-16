package com.mall.module.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.module.seckill.entity.SeckillRecord;
import org.apache.ibatis.annotations.Param;

public interface SeckillRecordMapper extends BaseMapper<SeckillRecord> {

    /** 原子扣减秒杀库存，成功返回1（防超卖） */
    int deductSecStock(@Param("itemId") Long itemId, @Param("qty") Integer qty);

    /** 增加已抢数量 */
    int incrSold(@Param("itemId") Long itemId, @Param("qty") Integer qty);
}