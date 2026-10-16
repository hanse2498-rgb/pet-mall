package com.mall.module.community.service;

import com.mall.common.PageResult;
import com.mall.module.community.dto.PostCreateRequest;
import com.mall.module.community.vo.PostCommentVO;
import com.mall.module.community.vo.PostDetailVO;
import com.mall.module.community.vo.PostVO;

import java.util.List;
import java.util.Map;

public interface CommunityService {

    /** 发布动态 */
    PostVO createPost(Long userId, PostCreateRequest request);

    /** 删除自己的动态 */
    void deletePost(Long userId, Long postId);

    /** 动态列表（viewerId 用于点赞态） */
    PageResult<PostVO> pagePosts(Long viewerId, String sort, Integer pageNum, Integer pageSize);

    /** 动态详情（增加浏览量） */
    PostDetailVO detail(Long viewerId, Long postId);

    /** 切换点赞 */
    Map<String, Object> toggleLike(Long userId, Long postId);

    /** 一级评论列表 */
    List<PostCommentVO> comments(Long postId);

    /** 发表评论 */
    PostCommentVO addComment(Long userId, Long postId, String content);

    /** 删除自己的评论 */
    void deleteComment(Long userId, Long commentId);
}