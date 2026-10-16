package com.mall.module.seckill.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品项
 */
@Data
@TableName("tb_seckill_item")
public class SeckillItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer seckillSold;

    /** 每人限购 */
    private Integer perUserLimit;

    /** 0停用 1启用 */
    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}