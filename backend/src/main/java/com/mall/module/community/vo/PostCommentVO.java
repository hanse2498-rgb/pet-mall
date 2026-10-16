package com.mall.module.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 VO
 */
@Data
public class PostCommentVO {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createTime;
}