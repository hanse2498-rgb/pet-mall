package com.mall.module.admin.controller;

import com.mall.common.Result;
import com.mall.module.product.entity.Category;
import com.mall.module.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台分类管理
 */
@Tag(name = "后台分类")
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表（含停用）")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.ok(categoryService.list());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        category.setId(null);
        category.setLevel(category.getParentId() == null || category.getParentId() == 0 ? 1 : 2);
        categoryService.save(category);
        return Result.ok(category);
    }

    @Operation(summary = "更新分类")
    @PutMapping
    public Result<Category> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.ok(category);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        categoryService.removeById(id);
        return Result.ok();
    }
}