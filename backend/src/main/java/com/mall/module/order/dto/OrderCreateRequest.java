package com.mall.module.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateRequest {

    /** 收货地址id */
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 订单备注 */
    private String remark;

    /** 幂等令牌（前端每次结算生成） */
    private String clientToken;

    /** 下单项 */
    @NotEmpty(message = "下单商品不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "SKU不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}