package com.mall.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 热销商品
 */
@Data
public class TopProductVO {
    private Long productId;
    private String name;
    private Long qty;
    private BigDecimal amount;
}
