package com.mall.module.dashboard.controller;

import com.mall.common.Result;
import com.mall.module.dashboard.service.DashboardService;
import com.mall.module.dashboard.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据看板
 */
@Tag(name = "后台数据看板")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "看板聚合数据")
    @GetMapping
    public Result<DashboardVO> overview(@RequestParam(defaultValue = "30") int range) {
        return Result.ok(dashboardService.overview(range));
    }
}
