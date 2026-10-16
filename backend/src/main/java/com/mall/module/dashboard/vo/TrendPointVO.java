package com.mall.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售趋势点（按天）
 */
@Data
public class TrendPointVO {
    private String date;
    private BigDecimal salesAmount;
    private Long orderCount;
}
