package com.mall.module.pet.dto;

import lombok.Data;

/**
 * 品种推荐项（后台编辑用）
 */
@Data
public class RecommendItemDTO {
    private Long productId;
    /** 1主粮 2其他 */
    private Integer type;
}
