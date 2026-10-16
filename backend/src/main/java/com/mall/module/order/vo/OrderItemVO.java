package com.mall.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单项（快照）
 */
@Data
public class OrderItemVO {

    private Long skuId;
    private String productName;
    private String skuImg;
    private String spec;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDate produceDate;
    private LocalDate expiryDate;
}