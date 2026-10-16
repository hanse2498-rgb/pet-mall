package com.mall.module.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物日常动态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_post")
public class Post extends BaseEntity {

    private Long userId;

    /** 关联宠物（可空） */
    private Long petId;

    private String content;

    /** 图片（JSON数组字符串） */
    private String images;

    private Integer likeCount;

    private Integer commentCount;

    private Integer viewCount;

    /** 0展示 1隐藏 */
    private Integer status;
}