package com.mall.module.review.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品评价分页 + 汇总
 */
@Data
public class ReviewPageVO {

    private Summary summary;
    /** 总数（基本类型，序列化为数字） */
    private long total;
    private List<ReviewVO> list;

    @Data
    public static class Summary {
        /** 平均分（保留1位） */
        private Double avgRating;
        /** 评价总数 */
        private long total;
        /** 好评率（4-5星占比，%) */
        private Double goodRate;
        private Long c5;
        private Long c4;
        private Long c3;
        private Long c2;
        private Long c1;
    }
}