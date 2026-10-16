package com.mall.module.community.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态 VO
 */
@Data
public class PostVO {

    private Long postId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private List<String> images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    /** 当前用户是否已点赞 */
    private Boolean liked;
    private LocalDateTime createTime;
}