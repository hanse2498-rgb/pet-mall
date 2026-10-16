package com.mall.module.product.vo;

import lombok.Data;

import java.util.List;

/**
 * 搜索联想结果：命中总数 + 少量候选项
 */
@Data
public class SuggestResultVO {
    private long total;
    private List<ProductSuggestVO> items;
}