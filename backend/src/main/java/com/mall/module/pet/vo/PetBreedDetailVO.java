package com.mall.module.pet.vo;

import com.mall.module.pet.entity.PetBreed;
import com.mall.module.product.vo.ProductCardVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 品种详情（含推荐商品）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetBreedDetailVO extends PetBreed {

    /** 推荐商品（手工映射，2 主粮 + 1 其他） */
    private List<ProductCardVO> recommendProducts;
}
