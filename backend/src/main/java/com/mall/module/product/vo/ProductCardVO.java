package com.mall.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品卡片（列表用）
 */
@Data
public class ProductCardVO {

    private Long id;
    private String name;
    private String subtitle;
    private String coverImg;
    private Integer saleType;
    private Integer isNearExpiry;
    private Integer sales;
    /** 最低售价（优先折扣价） */
    private BigDecimal minPrice;
    private String brandName;
    private String categoryName;
}