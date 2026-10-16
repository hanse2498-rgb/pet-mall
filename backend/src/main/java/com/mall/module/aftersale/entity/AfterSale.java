package com.mall.module.aftersale.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单（最简版：整单、审核通过即退款）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_after_sale")
public class AfterSale extends BaseEntity {

    private String afterSaleNo;

    private Long orderId;

    private Long userId;

    /** 售后原因 */
    private String reason;

    /** 补充说明 */
    private String description;

    /** 退款金额 */
    private BigDecimal amount;

    /** 0待审核 1已通过 2已拒绝 3已撤销 */
    private Integer status;

    private String adminRemark;

    private LocalDateTime auditTime;

    private LocalDateTime refundTime;
}
