package com.mall.module.rank.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 榜单查询的原始行（商品 + 近 7 天销量）
 */
@Data
public class RankRowVO {
    private Long productId;
    private String name;
    private String coverImg;
    private BigDecimal minPrice;
    private Integer sales7d;
    private Long categoryId;
    private String categoryName;
}
