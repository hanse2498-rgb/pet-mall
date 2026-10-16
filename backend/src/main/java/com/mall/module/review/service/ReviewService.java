package com.mall.module.review.service;

import com.mall.module.review.dto.ReviewCreateRequest;
import com.mall.module.review.vo.ReviewPageVO;

public interface ReviewService {

    /** 发表评价（校验：订单完成、属于本人、每订单商品一次） */
    void create(Long userId, ReviewCreateRequest request);

    /** 商品评价分页（含汇总） */
    ReviewPageVO pageByProduct(Long productId, Integer pageNum, Integer pageSize);

    /** 我的评价（含商品信息） */
    ReviewPageVO pageMine(Long userId, Integer pageNum, Integer pageSize);
}