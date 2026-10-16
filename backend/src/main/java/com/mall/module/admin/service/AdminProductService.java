package com.mall.module.admin.service;

import com.mall.common.PageResult;
import com.mall.module.admin.dto.AdminProductDTO;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.entity.SkuBatch;

import java.util.List;

/**
 * 后台商品管理
 */
public interface AdminProductService {

    PageResult<Product> pageProducts(String keyword, Long categoryId, Integer shelfStatus, Long brandId, Integer pageNum, Integer pageSize);

    Product create(AdminProductDTO.ProductForm form);

    Product update(AdminProductDTO.ProductForm form);

    void setShelf(Long id, Integer shelfStatus);

    void remove(Long id);

    List<Sku> skus(Long productId);

    Sku addSku(AdminProductDTO.SkuForm form);

    Sku updateSku(AdminProductDTO.SkuForm form);

    List<SkuBatch> batches(Long skuId);

    /** 新增批次：自动计算剩余占比 / 临期标记 / 临期自动7折 */
    SkuBatch addBatch(AdminProductDTO.BatchForm form);
}