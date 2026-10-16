package com.mall.module.order.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.order.service.OrderService;
import com.mall.module.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台订单管理
 */
@Tag(name = "后台订单")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表（全部用户）")
    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.adminPage(status, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable("id") Long id) {
        return Result.ok(orderService.adminDetail(id));
    }

    @Operation(summary = "发货")
    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable("id") Long id, @RequestBody ShipDTO dto) {
        orderService.adminShip(id, dto.getLogisticsCompany(), dto.getTrackingNo());
        return Result.ok();
    }

    @Data
    public static class ShipDTO {
        private String logisticsCompany;
        private String trackingNo;
    }
}