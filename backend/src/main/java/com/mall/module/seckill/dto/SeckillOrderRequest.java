package com.mall.module.seckill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀下单请求
 */
@Data
public class SeckillOrderRequest {

    @NotNull(message = "场次不能为空")
    private Long activityId;

    @NotNull(message = "秒杀商品不能为空")
    private Long itemId;

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}