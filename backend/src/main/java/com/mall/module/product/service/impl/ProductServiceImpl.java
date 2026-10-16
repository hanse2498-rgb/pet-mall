package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.common.ResultCode;
import com.mall.module.product.dto.ProductQuery;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuMapper;
import com.mall.module.product.service.ProductService;
import com.mall.module.product.vo.ProductCardVO;
import com.mall.module.product.vo.ProductDetailVO;
import com.mall.module.product.vo.SkuInfoVO;
import com.mall.module.product.vo.SuggestResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<ProductCardVO> pageCards(ProductQuery query) {
        IPage<ProductCardVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        productMapper.selectCardPage(page, query);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public ProductDetailVO detail(Long id) {
        ProductDetailVO vo = productMapper.selectDetail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }
        Product product = this.getById(id);
        vo.setImages(parseImages(product == null ? null : product.getImages()));
        vo.setSkus(queryActiveSkus(id));
        return vo;
    }

    @Override
    public PageResult<ProductCardVO> nearExpiry(ProductQuery query) {
        query.setSaleType(1);
        query.setIsNearExpiry(1);
        query.setSort("default");
        return pageCards(query);
    }

    @Override
    public SuggestResultVO suggest(String keyword) {
        SuggestResultVO result = new SuggestResultVO();
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            result.setTotal(0);
            result.setItems(Collections.emptyList());
            return result;
        }
        result.setTotal(productMapper.countSuggest(kw));
        result.setItems(productMapper.selectSuggest(kw, 6));
        return result;
    }

    @Override
    public SkuInfoVO skuInfo(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }
        Product product = this.getById(sku.getProductId());
        SkuInfoVO vo = new SkuInfoVO();
        vo.setSkuId(sku.getId());
        vo.setProductId(sku.getProductId());
        vo.setProductName(product == null ? "" : product.getName());
        vo.setCoverImg(product == null ? null : product.getCoverImg());
        vo.setSpecName(sku.getSpecName());
        vo.setPrice(effectivePrice(sku));
        vo.setOriginalPrice(sku.getOriginalPrice());
        vo.setIsDiscount(sku.getIsDiscount());
        vo.setStock(sku.getStock());
        vo.setIsNearExpiry(product == null ? 0 : product.getIsNearExpiry());
        vo.setShelfStatus(product == null ? 0 : product.getShelfStatus());
        return vo;
    }

    private BigDecimal effectivePrice(Sku sku) {
        if (sku.getIsDiscount() != null && sku.getIsDiscount() == 1 && sku.getDiscountPrice() != null) {
            return sku.getDiscountPrice();
        }
        return sku.getPrice();
    }

    private List<Sku> queryActiveSkus(Long productId) {
        return skuMapper.selectList(Wrappers.<Sku>lambdaQuery()
                .eq(Sku::getProductId, productId)
                .eq(Sku::getStatus, 1)
                .orderByAsc(Sku::getPrice));
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("解析商品图片失败, id={}, images={}", images, e.getMessage());
            return Collections.emptyList();
        }
    }
}