package com.mall.module.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车条目（含商品/SKU 信息）
 */
@Data
public class CartItemVO {

    private Long cartId;
    private Long skuId;
    private Long productId;
    private String productName;
    private String coverImg;
    private String specName;
    /** 成交价（折扣价优先） */
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer isDiscount;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
    private Integer isNearExpiry;
    /** 小计 = 成交价 × 数量 */
    private BigDecimal subtotal;

    public BigDecimal calculateSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}