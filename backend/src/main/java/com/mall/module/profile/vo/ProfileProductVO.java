package com.mall.module.profile.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 个人中心商品（收藏/足迹共用）
 */
@Data
public class ProfileProductVO {

    private Long productId;
    private String name;
    private String subtitle;
    private String coverImg;
    private BigDecimal minPrice;
    private Integer saleType;
    private Integer isNearExpiry;
    /** 收藏时间（收藏列表） */
    private LocalDateTime favoriteTime;
    /** 最近浏览时间（足迹列表） */
    private LocalDateTime lastViewTime;
    /** 浏览次数（足迹列表） */
    private Integer viewCount;
}