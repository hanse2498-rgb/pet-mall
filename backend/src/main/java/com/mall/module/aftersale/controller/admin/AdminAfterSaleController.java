package com.mall.module.aftersale.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.aftersale.dto.AfterSaleAuditDTO;
import com.mall.module.aftersale.service.AfterSaleService;
import com.mall.module.aftersale.vo.AfterSaleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 售后管理（后台）
 */
@Tag(name = "后台售后")
@RestController
@RequestMapping("/api/admin/after-sale")
@RequiredArgsConstructor
public class AdminAfterSaleController {

    private final AfterSaleService afterSaleService;

    @Operation(summary = "售后列表")
    @GetMapping
    public Result<PageResult<AfterSaleVO>> page(@RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(afterSaleService.adminPage(status, pageNum, pageSize));
    }

    @Operation(summary = "审核（通过=退款 / 拒绝）")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable("id") Long id, @RequestBody AfterSaleAuditDTO dto) {
        afterSaleService.audit(id, Boolean.TRUE.equals(dto.getPass()), dto.getRemark());
        return Result.ok();
    }
}
