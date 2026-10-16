package com.mall.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 搜索联想项（轻量字段）
 */
@Data
public class ProductSuggestVO {
    private Long id;
    private String name;
    private String coverImg;
    private BigDecimal minPrice;
    private String brandName;
}