package com.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_product")
public class Product extends BaseEntity {

    /** 品牌id */
    private Long brandId;

    /** 分类id */
    private Long categoryId;

    /** 商品名 */
    private String name;

    /** 副标题 */
    private String subtitle;

    /** 封面图 */
    private String coverImg;

    /** 轮播图（JSON数组字符串） */
    private String images;

    /** 详情富文本 */
    private String detailHtml;

    /** 类型：0普通 1食品 */
    private Integer saleType;

    /** 适用宠物：0通用 1猫 2狗 */
    private Integer petType;

    /** 是否临期：0否 1是 */
    private Integer isNearExpiry;

    /** 上架状态：0下架 1上架 */
    private Integer shelfStatus;

    /** 销量 */
    private Integer sales;
}