package com.mall.module.admin.controller;

import com.mall.common.Result;
import com.mall.module.product.entity.Brand;
import com.mall.module.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台品牌管理
 */
@Tag(name = "后台品牌")
@RestController
@RequestMapping("/api/admin/brand")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    @Operation(summary = "品牌列表（含停用）")
    @GetMapping
    public Result<List<Brand>> list() {
        return Result.ok(brandService.list());
    }

    @Operation(summary = "新增品牌")
    @PostMapping
    public Result<Brand> create(@RequestBody Brand brand) {
        brand.setId(null);
        brandService.save(brand);
        return Result.ok(brand);
    }

    @Operation(summary = "更新品牌")
    @PutMapping
    public Result<Brand> update(@RequestBody Brand brand) {
        brandService.updateById(brand);
        return Result.ok(brand);
    }

    @Operation(summary = "删除品牌")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        brandService.removeById(id);
        return Result.ok();
    }
}