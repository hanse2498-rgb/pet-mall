package com.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单明细（tb_order_item 无逻辑删除列）
 */
@Data
@TableName("tb_order_item")
public class OrderItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Long skuId;

    private String productName;

    private String skuImg;

    private String spec;

    /** 成交单价快照 */
    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    /** 发货批次 */
    private Long batchId;

    private LocalDate produceDate;

    private LocalDate expiryDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}