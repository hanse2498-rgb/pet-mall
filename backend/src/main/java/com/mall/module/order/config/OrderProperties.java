package com.mall.module.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "order")
public class OrderProperties {

    /** 待支付订单超时自动取消时间（分钟） */
    private int payTimeoutMinutes = 30;

    /** 超时扫描间隔（毫秒） */
    private long scanIntervalMs = 60000;
}