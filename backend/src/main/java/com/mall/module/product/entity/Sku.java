package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_sku")
public class Sku extends BaseEntity {

    /** 商品id */
    private Long productId;

    /** 规格名 */
    private String specName;

    /** 售价 */
    private BigDecimal price;

    /** 划线价 */
    private BigDecimal originalPrice;

    /** 是否折扣价：0否 1是 */
    private Integer isDiscount;

    /** 折扣价（临期7折落地） */
    private BigDecimal discountPrice;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 状态：0停售 1在售 */
    private Integer status;

    /** 乐观锁版本 */
    private Integer version;
}