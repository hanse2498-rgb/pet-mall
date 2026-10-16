package com.mall.module.review.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单条评价
 */
@Data
public class ReviewVO {

    private Long id;
    private Long productId;
    private String productName;
    private String coverImg;
    private String nickname;
    private String avatar;
    private String skuSpec;
    private Integer rating;
    private String content;
    private List<String> images;
    private LocalDateTime createTime;
}