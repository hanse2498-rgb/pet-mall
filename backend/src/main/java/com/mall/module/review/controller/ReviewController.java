package com.mall.module.review.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.review.dto.ReviewCreateRequest;
import com.mall.module.review.service.ReviewService;
import com.mall.module.review.vo.ReviewPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品评价
 */
@Tag(name = "商品评价")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "商品评价列表（含评分汇总）")
    @GetMapping("/product/{productId}")
    public Result<ReviewPageVO> pageByProduct(@PathVariable("productId") Long productId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(reviewService.pageByProduct(productId, pageNum, pageSize));
    }

    @Operation(summary = "我的评价")
    @GetMapping("/mine")
    public Result<ReviewPageVO> mine(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(reviewService.pageMine(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }

    @Operation(summary = "发表评价（需订单已完成）")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid ReviewCreateRequest request) {
        reviewService.create(StpUtil.getLoginIdAsLong(), request);
        return Result.ok();
    }
}