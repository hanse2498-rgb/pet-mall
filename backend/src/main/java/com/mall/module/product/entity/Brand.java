package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 品牌
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_brand")
public class Brand extends BaseEntity {

    /** 品牌名 */
    private String name;

    /** logo */
    private String logo;

    /** 描述 */
    private String description;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 排序 */
    private Integer sort;
}