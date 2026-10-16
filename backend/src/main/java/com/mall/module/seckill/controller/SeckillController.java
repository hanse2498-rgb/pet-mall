package com.mall.module.seckill.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.seckill.dto.SeckillOrderRequest;
import com.mall.module.seckill.service.SeckillService;
import com.mall.module.seckill.vo.SeckillActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 秒杀专区
 */
@Tag(name = "秒杀专区")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "当前秒杀场次（含商品与服务器时间）")
    @GetMapping("/current")
    public Result<SeckillActivityVO> current() {
        return Result.ok(seckillService.current());
    }

    @Operation(summary = "指定场次详情")
    @GetMapping("/activity/{id}")
    public Result<SeckillActivityVO> activity(@PathVariable("id") Long id) {
        return Result.ok(seckillService.activity(id));
    }

    @Operation(summary = "秒杀下单")
    @PostMapping("/order")
    public Result<Map<String, Object>> order(@RequestBody @Valid SeckillOrderRequest request) {
        StpUtil.checkLogin();
        Long orderId = seckillService.createOrder(StpUtil.getLoginIdAsLong(), request);
        return Result.ok(Map.of("orderId", orderId));
    }
}