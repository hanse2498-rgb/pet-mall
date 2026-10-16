package com.mall.module.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.module.admin.dto.AdminProductDTO;
import com.mall.module.admin.service.AdminProductService;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.entity.SkuBatch;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuBatchMapper;
import com.mall.module.product.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    /** 临期阈值：剩余保质期占比 <= 1/3 视为临期 */
    private static final BigDecimal NEAR_RATIO = new BigDecimal("0.3333");
    /** 临期折扣 */
    private static final BigDecimal NEAR_DISCOUNT = new BigDecimal("0.7");

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final SkuBatchMapper skuBatchMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<Product> pageProducts(String keyword, Long categoryId, Integer shelfStatus, Long brandId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapperLazy wrapper = new LambdaQueryWrapperLazy(keyword, categoryId, shelfStatus, brandId);
        IPage<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper.build());
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional
    public Product create(AdminProductDTO.ProductForm form) {
        Product product = toProduct(form);
        product.setId(null);
        product.setSales(0);
        product.setShelfStatus(form.getShelfStatus() == null ? 1 : form.getShelfStatus());
        product.setIsNearExpiry(0);
        productMapper.insert(product);
        return product;
    }

    @Override
    @Transactional
    public Product update(AdminProductDTO.ProductForm form) {
        if (form.getId() == null) {
            throw new BusinessException(400, "缺少商品id");
        }
        Product exist = productMapper.selectById(form.getId());
        if (exist == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        Product product = toProduct(form);
        product.setSales(exist.getSales());
        product.setCreateTime(exist.getCreateTime());
        productMapper.updateById(product);
        return product;
    }

    @Override
    @Transactional
    public void setShelf(Long id, Integer shelfStatus) {
        Product exist = mustExist(id);
        exist.setShelfStatus(shelfStatus == null ? 0 : shelfStatus);
        productMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        mustExist(id);
        productMapper.deleteById(id);
    }

    @Override
    public List<Sku> skus(Long productId) {
        return skuMapper.selectList(Wrappers.<Sku>lambdaQuery()
                .eq(Sku::getProductId, productId)
                .orderByAsc(Sku::getPrice));
    }

    @Override
    @Transactional
    public Sku addSku(AdminProductDTO.SkuForm form) {
        mustExist(form.getProductId());
        Sku sku = new Sku();
        applySku(sku, form);
        sku.setId(null);
        sku.setProductId(form.getProductId());
        skuMapper.insert(sku);
        return sku;
    }

    @Override
    @Transactional
    public Sku updateSku(AdminProductDTO.SkuForm form) {
        if (form.getId() == null) {
            throw new BusinessException(400, "缺少SKU id");
        }
        Sku exist = skuMapper.selectById(form.getId());
        if (exist == null) {
            throw new BusinessException(2001, "SKU不存在");
        }
        applySku(exist, form);
        skuMapper.updateById(exist);
        return exist;
    }

    @Override
    public List<SkuBatch> batches(Long skuId) {
        return skuBatchMapper.selectList(Wrappers.<SkuBatch>lambdaQuery()
                .eq(SkuBatch::getSkuId, skuId)
                .orderByDesc(SkuBatch::getCreateTime));
    }

    @Override
    @Transactional
    public SkuBatch addBatch(AdminProductDTO.BatchForm form) {
        if (form.getProduceDate() == null || form.getExpiryDays() == null || form.getExpiryDays() <= 0) {
            throw new BusinessException(400, "生产日期与保质期天数必填");
        }
        Sku sku = skuMapper.selectById(form.getSkuId());
        if (sku == null) {
            throw new BusinessException(2001, "SKU不存在");
        }
        SkuBatch batch = new SkuBatch();
        batch.setSkuId(form.getSkuId());
        batch.setBatchNo(form.getBatchNo());
        batch.setProduceDate(form.getProduceDate());
        batch.setExpiryDays(form.getExpiryDays());
        // 剩余保质期占比
        long daysSince = ChronoUnit.DAYS.between(form.getProduceDate(), LocalDate.now());
        BigDecimal remainRatio = BigDecimal.valueOf(form.getExpiryDays() - daysSince)
                .divide(BigDecimal.valueOf(form.getExpiryDays()), 4, RoundingMode.HALF_UP);
        remainRatio = remainRatio.max(BigDecimal.ZERO).min(BigDecimal.ONE);
        batch.setRemainRatio(remainRatio);
        boolean near = remainRatio.compareTo(NEAR_RATIO) <= 0;
        batch.setIsNearExpiry(near ? 1 : 0);
        skuBatchMapper.insert(batch);

        // 临期：SKU 打 7 折并标记商品临期
        if (near) {
            applyNearExpiry(sku);
        }
        // 重算商品临期标记（任一 SKU 存在临期批次即标记）
        refreshProductNearExpiry(sku.getProductId());
        return batch;
    }

    // ---------- helpers ----------

    private Product toProduct(AdminProductDTO.ProductForm form) {
        Product p = new Product();
        p.setId(form.getId());
        p.setBrandId(form.getBrandId());
        p.setCategoryId(form.getCategoryId());
        p.setName(form.getName());
        p.setSubtitle(form.getSubtitle());
        p.setCoverImg(form.getCoverImg());
        p.setImages(writeImages(form.getImages()));
        p.setDetailHtml(form.getDetailHtml());
        p.setSaleType(form.getSaleType() == null ? 0 : form.getSaleType());
        p.setPetType(form.getPetType() == null ? 0 : form.getPetType());
        return p;
    }

    private String writeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new BusinessException(400, "图片格式错误");
        }
    }

    private void applySku(Sku sku, AdminProductDTO.SkuForm form) {
        if (form.getSpecName() != null) sku.setSpecName(form.getSpecName());
        if (form.getPrice() != null) sku.setPrice(form.getPrice());
        if (form.getOriginalPrice() != null) sku.setOriginalPrice(form.getOriginalPrice());
        if (form.getStock() != null) sku.setStock(form.getStock());
        if (form.getStatus() != null) sku.setStatus(form.getStatus());
    }

    private Product mustExist(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        return exist;
    }

    /** 临期：SKU 打 7 折 */
    private void applyNearExpiry(Sku sku) {
        if (sku.getPrice() == null) return;
        BigDecimal discount = sku.getPrice().multiply(NEAR_DISCOUNT).setScale(2, RoundingMode.HALF_UP);
        sku.setIsDiscount(1);
        sku.setDiscountPrice(discount);
        skuMapper.updateById(sku);
    }

    /** 重算商品是否临期：任一 SKU 存在临期批次则为临期 */
    private void refreshProductNearExpiry(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return;
        List<Sku> skus = skuMapper.selectList(Wrappers.<Sku>lambdaQuery().eq(Sku::getProductId, productId));
        AtomicBoolean anyNear = new AtomicBoolean(false);
        for (Sku sku : skus) {
            Long near = skuBatchMapper.selectCount(Wrappers.<SkuBatch>lambdaQuery()
                    .eq(SkuBatch::getSkuId, sku.getId())
                    .eq(SkuBatch::getIsNearExpiry, 1));
            if (near != null && near > 0) {
                anyNear.set(true);
                break;
            }
        }
        product.setIsNearExpiry(anyNear.get() ? 1 : 0);
        productMapper.updateById(product);
    }

    /** 简单组合查询包装 */
    static class LambdaQueryWrapperLazy {
        private final String keyword;
        private final Long categoryId;
        private final Integer shelfStatus;
        private final Long brandId;

        LambdaQueryWrapperLazy(String keyword, Long categoryId, Integer shelfStatus, Long brandId) {
            this.keyword = keyword;
            this.categoryId = categoryId;
            this.shelfStatus = shelfStatus;
            this.brandId = brandId;
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> build() {
            var w = Wrappers.<Product>lambdaQuery();
            if (keyword != null && !keyword.isBlank()) {
                w.like(Product::getName, keyword);
            }
            if (categoryId != null) {
                w.and(q -> q.eq(Product::getCategoryId, categoryId)
                        .or().inSql(Product::getCategoryId,
                                "SELECT id FROM tb_category WHERE parent_id = " + categoryId));
            }
            if (shelfStatus != null) {
                w.eq(Product::getShelfStatus, shelfStatus);
            }
            if (brandId != null) {
                w.eq(Product::getBrandId, brandId);
            }
            w.orderByDesc(Product::getCreateTime);
            return w;
        }
    }
}