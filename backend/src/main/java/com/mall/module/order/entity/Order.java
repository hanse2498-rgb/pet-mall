package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_order")
public class Order extends BaseEntity {

    /** 订单号 */
    private String orderNo;

    private Long userId;

    /** 收货信息快照（JSON字符串） */
    private String receiver;

    /** 0待支付 1待发货 2待收货 3已完成 4已取消 5售后中 */
    private Integer status;

    private BigDecimal goodsAmount;

    private BigDecimal discountAmount;

    private BigDecimal freight;

    private BigDecimal payAmount;

    /** 0未支付 1已支付 2已退款 */
    private Integer payStatus;

    private String remark;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;
}