package com.mall.module.order.service.impl;

import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.module.order.entity.Order;
import com.mall.module.order.entity.Payment;
import com.mall.module.order.mapper.OrderMapper;
import com.mall.module.order.mapper.PaymentMapper;
import com.mall.module.order.service.PaymentService;
import com.mall.module.order.vo.PaymentResult;
import com.mall.module.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟支付实现（后续接入微信支付时替换 channel）
 */
@Service
@RequiredArgsConstructor
public class MockPaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final NoticeService noticeService;

    @Override
    @Transactional
    public PaymentResult pay(Long userId, Long orderId, boolean simulateFail, com.mall.module.order.dto.PayRequest req) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3002, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        Payment payment = paymentMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Payment>lambdaQuery()
                        .eq(Payment::getOrderId, orderId)
                        .orderByDesc(Payment::getCreateTime)
                        .last("limit 1"));
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setPayNo("P" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999));
            payment.setChannel(0);
            payment.setAmount(order.getPayAmount());
            payment.setStatus(0);
            paymentMapper.insert(payment);
        }

        PaymentResult result = new PaymentResult();
        result.setOrderId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setAmount(order.getPayAmount());

        if (simulateFail) {
            payment.setStatus(2);
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            result.setPaid(false);
            result.setMessage("模拟支付失败（演示）");
            return result;
        }

        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        order.setStatus(1);
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        result.setPaid(true);
        result.setMessage("模拟支付成功");
        noticeService.send(userId, 1, "支付成功",
                "订单 " + order.getOrderNo() + " 支付成功，金额 ¥" + order.getPayAmount() + "。",
                order.getId());
        return result;
    }
}