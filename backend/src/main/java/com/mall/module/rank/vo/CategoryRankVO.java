package com.mall.module.rank.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类榜单（每个分类 Top N）
 */
@Data
public class CategoryRankVO {
    private Long categoryId;
    private String categoryName;
    private List<RankItemVO> items;
}
