package com.mall.module.admin.controller;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.admin.dto.AdminProductDTO;
import com.mall.module.admin.service.AdminProductService;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.entity.SkuBatch;
import com.mall.module.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 后台商品管理
 */
@Tag(name = "后台商品")
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService productService;
    private final UploadService uploadService;

    @Operation(summary = "上传商品图片（保存到商品图片文件夹）")
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Long productId) {
        String url = uploadService.saveProductImage(file, productId);
        return Result.ok(Map.of("url", url));
    }

    @Operation(summary = "商品分页")
    @GetMapping
    public Result<PageResult<Product>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer shelfStatus,
                                            @RequestParam(required = false) Long brandId,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(productService.pageProducts(keyword, categoryId, shelfStatus, brandId, pageNum, pageSize));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Product> create(@RequestBody @Valid AdminProductDTO.ProductForm form) {
        return Result.ok(productService.create(form));
    }

    @Operation(summary = "更新商品")
    @PutMapping
    public Result<Product> update(@RequestBody @Valid AdminProductDTO.ProductForm form) {
        return Result.ok(productService.update(form));
    }

    @Operation(summary = "上架/下架")
    @PutMapping("/{id}/shelf")
    public Result<Void> setShelf(@PathVariable("id") Long id, @RequestBody ShelfDTO dto) {
        productService.setShelf(id, dto.getShelfStatus());
        return Result.ok();
    }

    @Operation(summary = "删除商品（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        productService.remove(id);
        return Result.ok();
    }

    @Operation(summary = "商品下的SKU")
    @GetMapping("/{id}/skus")
    public Result<List<Sku>> skus(@PathVariable("id") Long id) {
        return Result.ok(productService.skus(id));
    }

    @Operation(summary = "新增SKU")
    @PostMapping("/sku")
    public Result<Sku> addSku(@RequestBody AdminProductDTO.SkuForm form) {
        return Result.ok(productService.addSku(form));
    }

    @Operation(summary = "更新SKU")
    @PutMapping("/sku")
    public Result<Sku> updateSku(@RequestBody AdminProductDTO.SkuForm form) {
        return Result.ok(productService.updateSku(form));
    }

    @Operation(summary = "SKU批次列表")
    @GetMapping("/sku/{skuId}/batches")
    public Result<List<SkuBatch>> batches(@PathVariable("skuId") Long skuId) {
        return Result.ok(productService.batches(skuId));
    }

    @Operation(summary = "新增批次（自动算剩余占比+临期7折）")
    @PostMapping("/batch")
    public Result<SkuBatch> addBatch(@RequestBody AdminProductDTO.BatchForm form) {
        return Result.ok(productService.addBatch(form));
    }

    @Data
    public static class ShelfDTO {
        private Integer shelfStatus;
    }
}