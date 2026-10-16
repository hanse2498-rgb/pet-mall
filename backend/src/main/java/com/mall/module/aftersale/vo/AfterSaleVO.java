package com.mall.module.aftersale.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单展示
 */
@Data
public class AfterSaleVO {
    private Long id;
    private String afterSaleNo;
    private Long orderId;
    private String orderNo;
    private String reason;
    private String description;
    private BigDecimal amount;
    private Integer status;
    private String adminRemark;
    private LocalDateTime auditTime;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;
}
