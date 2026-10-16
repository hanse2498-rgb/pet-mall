package com.mall.module.review.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.module.member.entity.User;
import com.mall.module.member.mapper.UserMapper;
import com.mall.module.order.entity.Order;
import com.mall.module.order.entity.OrderItem;
import com.mall.module.order.mapper.OrderItemMapper;
import com.mall.module.order.mapper.OrderMapper;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuMapper;
import com.mall.module.review.dto.ReviewCreateRequest;
import com.mall.module.review.entity.Review;
import com.mall.module.review.mapper.ReviewMapper;
import com.mall.module.review.service.ReviewService;
import com.mall.module.review.vo.ReviewPageVO;
import com.mall.module.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void create(Long userId, ReviewCreateRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3002, "订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException(3001, "订单完成后才能评价");
        }
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId()));
        boolean hasProduct = items.stream().anyMatch(i -> i.getSkuId() != null
                && productsMatch(i, request.getProductId(), request.getSkuId(), skuMapper));
        if (!hasProduct) {
            throw new BusinessException(2001, "订单中不存在该商品");
        }
        Long exist = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderId, order.getId())
                .eq(Review::getProductId, request.getProductId()));
        if (exist != null && exist > 0) {
            throw new BusinessException(4003, "该订单商品已评价过");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(order.getId());
        review.setProductId(request.getProductId());
        review.setSkuId(request.getSkuId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(writeImages(request.getImages()));
        review.setStatus(0);
        reviewMapper.insert(review);
    }

    @Override
    public ReviewPageVO pageByProduct(Long productId, Integer pageNum, Integer pageSize) {
        ReviewPageVO vo = new ReviewPageVO();
        vo.setSummary(buildSummary(productId));

        IPage<Review> page = reviewMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Review>lambdaQuery()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 0)
                        .orderByDesc(Review::getCreateTime));
        vo.setTotal(page.getTotal());
        vo.setList(buildVOs(page.getRecords()));
        return vo;
    }

    @Override
    public ReviewPageVO pageMine(Long userId, Integer pageNum, Integer pageSize) {
        IPage<Review> page = reviewMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Review>lambdaQuery()
                        .eq(Review::getUserId, userId)
                        .eq(Review::getStatus, 0)
                        .orderByDesc(Review::getCreateTime));
        ReviewPageVO vo = new ReviewPageVO();
        vo.setSummary(buildSummaryOf(userId));
        vo.setTotal(page.getTotal());
        vo.setList(buildVOs(page.getRecords()));
        return vo;
    }

    // ---------- helpers ----------

    private boolean productsMatch(OrderItem item, Long productId, Long skuId, SkuMapper skuMapper) {
        if (item.getSkuId() == null) {
            return false;
        }
        // 以 skuId 匹配为主；若订单项无则可忽略
        return skuId != null && item.getSkuId().equals(skuId);
    }

    private ReviewPageVO.Summary buildSummary(Long productId) {
        return buildSummaryOf(Wrappers.<Review>lambdaQuery()
                .select(Review::getRating)
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, 0));
    }

    private ReviewPageVO.Summary buildSummaryOf(Long userId) {
        return buildSummaryOf(Wrappers.<Review>lambdaQuery()
                .select(Review::getRating)
                .eq(Review::getUserId, userId)
                .eq(Review::getStatus, 0));
    }

    private ReviewPageVO.Summary buildSummaryOf(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review> wrapper) {
        List<Map<String, Object>> rows = reviewMapper.selectMaps(wrapper);
        ReviewPageVO.Summary s = new ReviewPageVO.Summary();
        long total = rows.size();
        long c5 = 0, c4 = 0, c3 = 0, c2 = 0, c1 = 0;
        long sum = 0;
        for (Map<String, Object> row : rows) {
            Object r = row.values().iterator().next();
            int rating = r instanceof Number ? ((Number) r).intValue() : Integer.parseInt(String.valueOf(r));
            sum += rating;
            switch (rating) {
                case 5 -> c5++;
                case 4 -> c4++;
                case 3 -> c3++;
                case 2 -> c2++;
                default -> c1++;
            }
        }
        s.setTotal(total);
        s.setAvgRating(total == 0 ? 0.0 : Math.round(sum * 10.0 / total) / 10.0);
        s.setGoodRate(total == 0 ? 100.0 : Math.round((c5 + c4) * 1000.0 / total) / 10.0);
        s.setC5(c5);
        s.setC4(c4);
        s.setC3(c3);
        s.setC2(c2);
        s.setC1(c1);
        return s;
    }

    private List<ReviewVO> buildVOs(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = reviews.stream().map(Review::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        List<Long> productIds = reviews.stream().map(Review::getProductId).distinct().toList();
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Long> skuIds = reviews.stream().map(Review::getSkuId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, Sku> skuMap = skuIds.isEmpty() ? Map.of()
                : skuMapper.selectBatchIds(skuIds).stream()
                        .collect(Collectors.toMap(Sku::getId, Function.identity()));

        return reviews.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId());
            vo.setProductId(r.getProductId());
            Product product = productMap.get(r.getProductId());
            vo.setProductName(product == null ? null : product.getName());
            vo.setCoverImg(product == null ? null : product.getCoverImg());
            User user = userMap.get(r.getUserId());
            vo.setNickname(mask(user == null ? "匿名用户" : user.getNickname()));
            vo.setAvatar(user == null ? null : user.getAvatar());
            Sku sku = r.getSkuId() == null ? null : skuMap.get(r.getSkuId());
            vo.setSkuSpec(sku == null ? null : sku.getSpecName());
            vo.setRating(r.getRating());
            vo.setContent(r.getContent());
            vo.setImages(parseImages(r.getImages()));
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private String mask(String nickname) {
        if (nickname == null || nickname.length() <= 1) {
            return nickname == null ? "匿名用户" : nickname;
        }
        return nickname.substring(0, 1) + "***";
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("解析评价图片失败: {}", e.getMessage());
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