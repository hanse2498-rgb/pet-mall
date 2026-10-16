package com.mall.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付结果
 */
@Data
public class PaymentResult {

    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private Boolean paid;
    private String message;
}