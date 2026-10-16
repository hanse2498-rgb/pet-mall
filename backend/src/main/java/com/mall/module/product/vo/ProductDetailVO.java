package com.mall.module.product.vo;

import com.mall.module.product.entity.Sku;
import lombok.Data;

import java.util.List;

/**
 * 商品详情
 */
@Data
public class ProductDetailVO {

    private Long id;
    private String name;
    private String subtitle;
    private String coverImg;
    /** 轮播图 */
    private List<String> images;
    /** 详情富文本 */
    private String detailHtml;
    private Integer saleType;
    private Integer isNearExpiry;
    private Integer shelfStatus;
    private Integer sales;
    private String brandName;
    private String categoryName;
    /** 可售 SKU */
    private List<Sku> skus;
}