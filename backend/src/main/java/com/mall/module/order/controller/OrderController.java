package com.mall.module.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.order.dto.OrderCreateRequest;
import com.mall.module.order.dto.PayRequest;
import com.mall.module.order.service.OrderService;
import com.mall.module.order.service.PaymentService;
import com.mall.module.order.vo.OrderVO;
import com.mall.module.order.vo.PaymentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@Tag(name = "订单")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> create(@RequestBody @Valid OrderCreateRequest request) {
        Long orderId = orderService.createOrder(StpUtil.getLoginIdAsLong(), request);
        return Result.ok(orderService.detail(StpUtil.getLoginIdAsLong(), orderId));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.page(StpUtil.getLoginIdAsLong(), status, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable("id") Long id) {
        return Result.ok(orderService.detail(StpUtil.getLoginIdAsLong(), id));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        orderService.cancel(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable("id") Long id) {
        orderService.confirmReceive(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "模拟支付")
    @PostMapping("/{id}/pay")
    public Result<PaymentResult> pay(@PathVariable("id") Long id, @RequestBody(required = false) PayRequest request) {
        boolean fail = request != null && Boolean.TRUE.equals(request.getSimulateFail());
        return Result.ok(paymentService.pay(StpUtil.getLoginIdAsLong(), id, fail, request));
    }
}