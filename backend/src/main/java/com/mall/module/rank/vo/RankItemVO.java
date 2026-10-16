package com.mall.module.rank.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 榜单条目
 */
@Data
public class RankItemVO {
    private int rank;
    private Long productId;
    private String name;
    private String coverImg;
    private BigDecimal price;
    private Integer sales7d;
}
