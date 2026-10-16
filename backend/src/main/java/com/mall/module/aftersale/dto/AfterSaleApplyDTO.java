package com.mall.module.aftersale.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请售后
 */
@Data
public class AfterSaleApplyDTO {

    @NotNull(message = "订单不能为空")
    private Long orderId;

    private String reason;

    private String description;
}
