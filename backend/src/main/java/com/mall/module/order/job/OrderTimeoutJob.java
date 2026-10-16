package com.mall.module.order.job;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.module.order.config.OrderProperties;
import com.mall.module.order.entity.Order;
import com.mall.module.order.service.OrderService;
import com.mall.module.order.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单定时任务：待支付订单超时自动取消（回补库存）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderService orderService;
    private final OrderProperties properties;

    @Scheduled(fixedDelayString = "${order.scan-interval-ms:60000}",
            initialDelayString = "10000")
    public void cancelTimeoutOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(properties.getPayTimeoutMinutes());
        long batchSize = 100;
        int cancelled = 0;

        IPage<Order> page = new Page<>(1, batchSize);
        do {
            page = orderService.page(page, Wrappers.<Order>lambdaQuery()
                    .eq(Order::getStatus, OrderServiceImpl.STATUS_PENDING_PAY)
                    .lt(Order::getCreateTime, cutoff)
                    .orderByAsc(Order::getCreateTime));
            for (Order order : page.getRecords()) {
                try {
                    orderService.cancel(order.getUserId(), order.getId());
                    cancelled++;
                } catch (Exception e) {
                    log.warn("订单超时取消失败, orderId={}, err={}", order.getId(), e.getMessage());
                }
            }
        } while (page.getCurrent() * page.getSize() < page.getTotal());

        if (cancelled > 0) {
            log.info("订单超时自动取消完成，共取消 {} 单（阈值 {} 分钟）", cancelled, properties.getPayTimeoutMinutes());
        }
    }
}