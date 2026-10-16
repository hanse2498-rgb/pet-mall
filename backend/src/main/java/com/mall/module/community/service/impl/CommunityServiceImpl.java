package com.mall.module.community.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.module.community.dto.PostCreateRequest;
import com.mall.module.community.entity.Post;
import com.mall.module.community.entity.PostComment;
import com.mall.module.community.entity.PostLike;
import com.mall.module.community.mapper.PostCommentMapper;
import com.mall.module.community.mapper.PostLikeMapper;
import com.mall.module.community.mapper.PostMapper;
import com.mall.module.community.service.CommunityService;
import com.mall.module.community.vo.PostCommentVO;
import com.mall.module.community.vo.PostDetailVO;
import com.mall.module.community.vo.PostVO;
import com.mall.module.member.entity.User;
import com.mall.module.member.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PostVO createPost(Long userId, PostCreateRequest request) {
        if ((request.getContent() == null || request.getContent().isBlank())
                && (request.getImages() == null || request.getImages().isEmpty())) {
            throw new BusinessException(400, "写点什么或传张图吧");
        }
        if (request.getImages() != null && request.getImages().size() > 9) {
            throw new BusinessException(400, "图片最多9张");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setPetId(request.getPetId());
        post.setContent(request.getContent());
        post.setImages(writeImages(request.getImages()));
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setStatus(0);
        postMapper.insert(post);
        return toVO(post, false, userMapper.selectById(userId));
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = mustPost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(4001, "只能删除自己的动态");
        }
        postMapper.deleteById(postId);
    }

    @Override
    public PageResult<PostVO> pagePosts(Long viewerId, String sort, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<Post>lambdaQuery()
                .eq(Post::getStatus, 0);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getLikeCount).orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getViewCount).orderByDesc(Post::getCreateTime);
        } else {
            wrapper.orderByDesc(Post::getCreateTime);
        }
        IPage<Post> page = postMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Post> records = page.getRecords();
        List<PostVO> list = records.isEmpty() ? List.of()
                : buildVOs(viewerId, records);
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    @Transactional
    public PostDetailVO detail(Long viewerId, Long postId) {
        Post post = mustPost(postId);
        post.setViewCount(post.getViewCount() == null ? 0 : post.getViewCount() + 1);
        postMapper.updateById(post);
        PostDetailVO vo = new PostDetailVO();
        copyToVO(post, vo, isLiked(viewerId, postId), userMapper.selectById(post.getUserId()));
        vo.setComments(comments(postId));
        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        mustPost(postId);
        PostLike exist = postLikeMapper.selectOne(Wrappers.<PostLike>lambdaQuery()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId));
        boolean liked;
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            postLikeMapper.insert(like);
            liked = true;
        }
        Long count = postLikeMapper.selectCount(Wrappers.<PostLike>lambdaQuery()
                .eq(PostLike::getPostId, postId));
        Post post = postMapper.selectById(postId);
        post.setLikeCount(count == null ? 0 : count.intValue());
        postMapper.updateById(post);
        return Map.of("liked", liked, "likeCount", post.getLikeCount());
    }

    @Override
    public List<PostCommentVO> comments(Long postId) {
        List<PostComment> comments = postCommentMapper.selectList(Wrappers.<PostComment>lambdaQuery()
                .eq(PostComment::getPostId, postId)
                .orderByAsc(PostComment::getCreateTime));
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = comments.stream().map(PostComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        return comments.stream().map(c -> {
            PostCommentVO vo = new PostCommentVO();
            vo.setCommentId(c.getId());
            vo.setUserId(c.getUserId());
            User user = userMap.get(c.getUserId());
            vo.setNickname(user == null ? "匿名用户" : user.getNickname());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostCommentVO addComment(Long userId, Long postId, String content) {
        Post post = mustPost(postId);
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        postCommentMapper.insert(comment);
        post.setCommentCount(post.getCommentCount() == null ? 0 : post.getCommentCount() + 1);
        postMapper.updateById(post);
        PostCommentVO vo = new PostCommentVO();
        vo.setCommentId(comment.getId());
        vo.setUserId(userId);
        User user = userMapper.selectById(userId);
        vo.setNickname(user == null ? "匿名用户" : user.getNickname());
        vo.setContent(content);
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(4004, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(4004, "只能删除自己的评论");
        }
        postCommentMapper.deleteById(commentId);
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, (post.getCommentCount() == null ? 0 : post.getCommentCount()) - 1));
            postMapper.updateById(post);
        }
    }

    // ---------- helpers ----------

    private Post mustPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(4002, "动态不存在");
        }
        return post;
    }

    private List<PostVO> buildVOs(Long viewerId, List<Post> posts) {
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        Set<Long> likedIds = viewerId == null ? Set.of()
                : postLikeMapper.selectList(Wrappers.<PostLike>lambdaQuery()
                                .eq(PostLike::getUserId, viewerId)
                                .in(PostLike::getPostId, posts.stream().map(Post::getId).toList()))
                        .stream().map(PostLike::getPostId).collect(Collectors.toSet());
        return posts.stream()
                .map(p -> toVO(p, likedIds.contains(p.getId()), userMap.get(p.getUserId())))
                .collect(Collectors.toList());
    }

    private PostVO toVO(Post post, boolean liked, User user) {
        PostVO vo = new PostVO();
        copyToVO(post, vo, liked, user);
        return vo;
    }

    private void copyToVO(Post post, PostVO vo, boolean liked, User user) {
        vo.setPostId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setNickname(user == null ? "匿名用户" : user.getNickname());
        vo.setAvatar(user == null ? null : user.getAvatar());
        vo.setContent(post.getContent());
        vo.setImages(parseImages(post.getImages()));
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setViewCount(post.getViewCount());
        vo.setLiked(liked);
        vo.setCreateTime(post.getCreateTime());
    }

    private boolean isLiked(Long viewerId, Long postId) {
        if (viewerId == null) {
            return false;
        }
        Long count = postLikeMapper.selectCount(Wrappers.<PostLike>lambdaQuery()
                .eq(PostLike::getUserId, viewerId)
                .eq(PostLike::getPostId, postId));
        return count != null && count > 0;
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("解析动态图片失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String writeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new BusinessException(400, "图片格式错误");
        }
    }
}