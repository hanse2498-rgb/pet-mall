package com.mall.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类销售占比
 */
@Data
public class CategoryShareVO {
    private String categoryName;
    private BigDecimal amount;
    private Long qty;
}
