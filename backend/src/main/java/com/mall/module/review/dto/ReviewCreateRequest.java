package com.mall.module.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发表评价请求
 */
@Data
public class ReviewCreateRequest {

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "商品不能为空")
    private Long productId;

    private Long skuId;

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分1-5")
    @Max(value = 5, message = "评分1-5")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    /** 图片URL列表（可选） */
    private List<String> images;
}