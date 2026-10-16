package com.mall.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 直购信息（立即购买用）
 */
@Data
public class SkuInfoVO {

    private Long skuId;
    private Long productId;
    private String productName;
    private String coverImg;
    private String specName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer isDiscount;
    private Integer stock;
    private Integer isNearExpiry;
    private Integer shelfStatus;
}