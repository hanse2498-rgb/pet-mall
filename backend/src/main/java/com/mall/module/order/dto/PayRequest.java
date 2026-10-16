package com.mall.module.order.dto;

import lombok.Data;

/**
 * 支付请求（模拟）
 */
@Data
public class PayRequest {

    /** 仅用于演示：true 表示模拟支付失败 */
    private Boolean simulateFail = false;
}