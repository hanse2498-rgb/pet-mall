package com.mall.module.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.community.dto.CommentCreateRequest;
import com.mall.module.community.dto.PostCreateRequest;
import com.mall.module.community.service.CommunityService;
import com.mall.module.community.vo.PostCommentVO;
import com.mall.module.community.vo.PostDetailVO;
import com.mall.module.community.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宠物社区（浏览公开，写操作需登录）
 */
@Tag(name = "宠物社区")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Validated
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "动态列表（latest/hot）")
    @GetMapping("/posts")
    public Result<PageResult<PostVO>> posts(@RequestParam(defaultValue = "latest") String sort,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long viewerId = StpUtil.getLoginIdDefaultNull() == null ? null : StpUtil.getLoginIdAsLong();
        return Result.ok(communityService.pagePosts(viewerId, sort, pageNum, pageSize));
    }

    @Operation(summary = "动态详情（浏览+增加浏览数）")
    @GetMapping("/posts/{id}")
    public Result<PostDetailVO> detail(@PathVariable("id") Long id) {
        Long viewerId = StpUtil.getLoginIdDefaultNull() == null ? null : StpUtil.getLoginIdAsLong();
        return Result.ok(communityService.detail(viewerId, id));
    }

    @Operation(summary = "评论列表")
    @GetMapping("/posts/{id}/comments")
    public Result<List<PostCommentVO>> comments(@PathVariable("id") Long id) {
        return Result.ok(communityService.comments(id));
    }

    @Operation(summary = "发布动态")
    @PostMapping("/post")
    public Result<PostVO> create(@RequestBody @Validated PostCreateRequest request) {
        StpUtil.checkLogin();
        return Result.ok(communityService.createPost(StpUtil.getLoginIdAsLong(), request));
    }

    @Operation(summary = "删除自己的动态")
    @DeleteMapping("/post/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        StpUtil.checkLogin();
        communityService.deletePost(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/post/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable("id") Long id) {
        StpUtil.checkLogin();
        return Result.ok(communityService.toggleLike(StpUtil.getLoginIdAsLong(), id));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/post/{id}/comment")
    public Result<PostCommentVO> comment(@PathVariable("id") Long id,
                                         @RequestBody @Valid CommentCreateRequest request) {
        StpUtil.checkLogin();
        return Result.ok(communityService.addComment(StpUtil.getLoginIdAsLong(), id, request.getContent()));
    }

    @Operation(summary = "删除自己的评论")
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable("id") Long id) {
        StpUtil.checkLogin();
        communityService.deleteComment(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }
}