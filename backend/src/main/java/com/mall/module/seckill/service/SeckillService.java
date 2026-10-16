package com.mall.module.seckill.service;

import com.mall.module.seckill.dto.SeckillOrderRequest;
import com.mall.module.seckill.vo.SeckillActivityVO;

public interface SeckillService {

    /** 当前可参与/即将开始的场次 */
    SeckillActivityVO current();

    /** 指定场次详情 */
    SeckillActivityVO activity(Long activityId);

    /** 秒杀下单，返回订单id */
    Long createOrder(Long userId, SeckillOrderRequest request);
}