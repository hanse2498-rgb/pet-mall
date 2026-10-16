package com.mall.module.aftersale.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.aftersale.dto.AfterSaleApplyDTO;
import com.mall.module.aftersale.service.AfterSaleService;
import com.mall.module.aftersale.vo.AfterSaleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 售后（前台）
 */
@Tag(name = "售后")
@RestController
@RequestMapping("/api/after-sale")
@RequiredArgsConstructor
public class AfterSaleController {

    private final AfterSaleService afterSaleService;

    @Operation(summary = "申请售后")
    @PostMapping
    public Result<Long> apply(@RequestBody @Valid AfterSaleApplyDTO dto) {
        return Result.ok(afterSaleService.apply(
                StpUtil.getLoginIdAsLong(), dto.getOrderId(), dto.getReason(), dto.getDescription()));
    }

    @Operation(summary = "我的售后列表")
    @GetMapping
    public Result<PageResult<AfterSaleVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(afterSaleService.pageMine(StpUtil.getLoginIdAsLong(), pageNum, pageSize));
    }

    @Operation(summary = "售后详情")
    @GetMapping("/{id}")
    public Result<AfterSaleVO> detail(@PathVariable("id") Long id) {
        return Result.ok(afterSaleService.detail(StpUtil.getLoginIdAsLong(), id));
    }

    @Operation(summary = "撤销售后申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        afterSaleService.cancel(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }
}
