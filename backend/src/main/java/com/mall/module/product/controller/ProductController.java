package com.mall.module.product.controller;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.product.dto.ProductQuery;
import com.mall.module.product.entity.Brand;
import com.mall.module.product.service.BrandService;
import com.mall.module.product.service.CategoryService;
import com.mall.module.product.service.ProductService;
import com.mall.module.product.vo.CategoryVO;
import com.mall.module.product.vo.ProductCardVO;
import com.mall.module.product.vo.ProductDetailVO;
import com.mall.module.product.vo.SkuInfoVO;
import com.mall.module.product.vo.SuggestResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品前台接口
 */
@Tag(name = "商品")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final BrandService brandService;
    private final CategoryService categoryService;

    @Operation(summary = "分类树")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.ok(categoryService.tree());
    }

    @Operation(summary = "品牌列表")
    @GetMapping("/brands")
    public Result<List<Brand>> brands() {
        return Result.ok(brandService.listActive());
    }

    @Operation(summary = "商品列表（筛选/排序/分页）")
    @GetMapping("/list")
    public Result<PageResult<ProductCardVO>> list(ProductQuery query) {
        return Result.ok(productService.pageCards(query));
    }

    @Operation(summary = "临期专区")
    @GetMapping("/near-expiry")
    public Result<PageResult<ProductCardVO>> nearExpiry(ProductQuery query) {
        return Result.ok(productService.nearExpiry(query));
    }

    @Operation(summary = "搜索联想（在售商品）")
    @GetMapping("/suggest")
    public Result<SuggestResultVO> suggest(@RequestParam("q") String q) {
        return Result.ok(productService.suggest(q));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable("id") Long id) {
        return Result.ok(productService.detail(id));
    }

    @Operation(summary = "SKU 直购信息（立即购买）")
    @GetMapping("/sku/{skuId}")
    public Result<SkuInfoVO> skuInfo(@PathVariable("skuId") Long skuId) {
        return Result.ok(productService.skuInfo(skuId));
    }
}