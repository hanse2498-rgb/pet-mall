package com.mall.module.order.service;

import com.mall.module.order.dto.PayRequest;
import com.mall.module.order.vo.PaymentResult;

/**
 * 支付服务（当前为模拟实现，预留微信支付位）
 */
public interface PaymentService {

    /**
     * 发起支付
     *
     * @param simulateFail 是否模拟失败
     */
    PaymentResult pay(Long userId, Long orderId, boolean simulateFail, PayRequest req);
}