package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SKU 批次（食品）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_sku_batch")
public class SkuBatch extends BaseEntity {

    /** SKU id */
    private Long skuId;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate produceDate;

    /** 保质期（天） */
    private Integer expiryDays;

    /** 剩余保质期占比 */
    private BigDecimal remainRatio;

    /** 是否临期：0否 1是 */
    private Integer isNearExpiry;
}