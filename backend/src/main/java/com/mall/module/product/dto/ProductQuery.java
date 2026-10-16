package com.mall.module.product.dto;

import lombok.Data;

/**
 * 商品列表查询参数
 */
@Data
public class ProductQuery {

    /** 关键词（名称/副标题） */
    private String keyword;

    /** 分类id（含其子分类） */
    private Long categoryId;

    /** 品牌id */
    private Long brandId;

    /** 价格下限 */
    private java.math.BigDecimal priceMin;

    /** 价格上限 */
    private java.math.BigDecimal priceMax;

    /** 类型：0普通 1食品 */
    private Integer saleType;

    /** 适用宠物：0通用 1猫 2狗 */
    private Integer petType;

    /** 是否临期：0否 1是 */
    private Integer isNearExpiry;

    /** 排序：default/sales/price_asc/price_desc/new */
    private String sort = "default";

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 12;
}