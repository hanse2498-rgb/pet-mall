package com.mall.module.cart.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.cart.service.CartService;
import com.mall.module.cart.vo.CartItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车
 */
@Tag(name = "购物车")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "勾选购物车项（结算用）")
    @GetMapping("/selected")
    public Result<List<CartItemVO>> selected() {
        return Result.ok(cartService.listSelected(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "购物车件数")
    @GetMapping("/count")
    public Result<Map<String, Object>> count() {
        return Result.ok(Map.of("count", cartService.count(StpUtil.getLoginIdAsLong())));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<CartItemVO> add(@RequestBody @jakarta.validation.Valid AddDTO dto) {
        return Result.ok(cartService.add(StpUtil.getLoginIdAsLong(), dto.getSkuId(), dto.getQuantity()));
    }

    @Operation(summary = "更新数量或勾选")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestParam(required = false) Integer quantity,
                               @RequestParam(required = false) Integer selected) {
        cartService.update(StpUtil.getLoginIdAsLong(), id, quantity, selected);
        return Result.ok();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        cartService.remove(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "批量移除（按SKU，支付成功后清理）")
    @PostMapping("/remove-batch")
    public Result<Void> removeBatch(@RequestBody List<Long> skuIds) {
        cartService.removeBySkuIds(StpUtil.getLoginIdAsLong(), skuIds);
        return Result.ok();
    }

    @Data
    public static class AddDTO {
        @NotNull(message = "SKU不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}