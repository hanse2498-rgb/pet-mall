package com.mall.module.community.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 动态详情（含评论）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailVO extends PostVO {

    private List<PostCommentVO> comments;
}