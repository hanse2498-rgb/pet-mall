package com.mall.module.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物品种图鉴
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_pet_breed")
public class PetBreed extends BaseEntity {

    /** 1猫 2狗 */
    private Integer type;

    private String name;

    private String enName;

    private String coverImg;

    /** 一句话简介 */
    private String intro;

    /** 详细介绍 */
    private String description;

    /** 生活习性 */
    private String habits;

    /** 外观与颜色 */
    private String appearance;

    /** 饲养建议 */
    private String careTips;

    private String size;

    private String hair;

    /** 性格 */
    private String characterTrait;

    private String origin;

    private String lifeSpan;

    /** 饲养难度 1-3 */
    private Integer difficulty;

    private Integer sort;

    /** 0下架 1上架 */
    private Integer status;
}
