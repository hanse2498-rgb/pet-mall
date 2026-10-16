package com.mall.module.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀场次 VO（含商品）
 */
@Data
public class SeckillActivityVO {

    private Long activityId;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 0未开始 1进行中 2已结束 */
    private Integer state;
    private LocalDateTime serverTime;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long itemId;
        private Long skuId;
        private Long productId;
        private String productName;
        private String coverImg;
        private String specName;
        private BigDecimal normalPrice;
        private BigDecimal seckillPrice;
        private Integer seckillStock;
        private Integer seckillSold;
        /** 已抢百分比 0-100 */
        private Integer percent;
        private Integer perUserLimit;
    }
}