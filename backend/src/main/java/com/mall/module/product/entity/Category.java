package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_category")
public class Category extends BaseEntity {

    /** 父分类id，0为一级 */
    private Long parentId;

    /** 分类名 */
    private String name;

    /** 层级 */
    private Integer level;

    /** 排序 */
    private Integer sort;

    /** 状态：0禁用 1启用 */
    private Integer status;
}