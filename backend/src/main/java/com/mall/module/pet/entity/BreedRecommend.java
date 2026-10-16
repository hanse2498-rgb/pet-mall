package com.mall.module.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 品种推荐商品（手工映射）
 */
@Data
@TableName("tb_breed_recommend")
public class BreedRecommend implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long breedId;

    private Long productId;

    /** 1主粮 2其他 */
    private Integer type;

    private Integer sort;
}
