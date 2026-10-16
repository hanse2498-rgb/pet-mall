package com.mall.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图
 */
@Data
public class OrderVO {

    private Long orderId;
    private String orderNo;
    private Integer status;
    private BigDecimal goodsAmount;
    private BigDecimal discountAmount;
    private BigDecimal freight;
    private BigDecimal payAmount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private List<OrderItemVO> items;

    /** 以下字段仅详情接口返回 */
    private String receiver;
    private String payNo;
    private Integer payStatus;
    private LocalDateTime paymentTime;
    private String logisticsCompany;
    private String trackingNo;
}