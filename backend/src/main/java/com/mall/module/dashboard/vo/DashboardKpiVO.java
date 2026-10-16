package com.mall.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 看板核心指标
 */
@Data
public class DashboardKpiVO {
    /** 销售额（已支付，含退款，不扣减） */
    private BigDecimal salesAmount;
    /** 下单数 */
    private Long orderCount;
    /** 已支付订单数 */
    private Long paidOrderCount;
    /** 客单价 */
    private BigDecimal avgPrice;
    /** 退款金额 */
    private BigDecimal refundAmount;
    /** 新增用户 */
    private Long newUsers;
    /** 待发货订单 */
    private Long pendingShip;
    /** 待审核售后 */
    private Long pendingAfterSale;
}
