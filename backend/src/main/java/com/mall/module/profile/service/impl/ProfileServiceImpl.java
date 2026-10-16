package com.mall.module.profile.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mall.common.BusinessException;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuMapper;
import com.mall.module.profile.entity.Favorite;
import com.mall.module.profile.entity.Footprint;
import com.mall.module.profile.mapper.FavoriteMapper;
import com.mall.module.profile.mapper.FootprintMapper;
import com.mall.module.profile.service.ProfileService;
import com.mall.module.profile.vo.ProfileProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final FavoriteMapper favoriteMapper;
    private final FootprintMapper footprintMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    @Override
    public List<ProfileProductVO> listFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        if (favorites.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = favorites.stream().map(Favorite::getProductId).toList();
        Map<Long, Product> products = loadProducts(productIds);
        Map<Long, BigDecimal> minPrices = minPrices(productIds);
        return favorites.stream().map(f -> {
            ProfileProductVO vo = toProfileVO(products.get(f.getProductId()), minPrices.get(f.getProductId()));
            vo.setFavoriteTime(f.getCreateTime());
            return vo;
        }).filter(v -> v.getProductId() != null).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        mustProduct(productId);
        Favorite exist = favoriteMapper.selectOne(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
        return true;
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        Long count = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void recordFootprint(Long userId, Long productId) {
        mustProduct(productId);
        Footprint exist = footprintMapper.selectOne(Wrappers.<Footprint>lambdaQuery()
                .eq(Footprint::getUserId, userId)
                .eq(Footprint::getProductId, productId));
        LocalDateTime now = LocalDateTime.now();
        if (exist != null) {
            exist.setViewCount(exist.getViewCount() == null ? 1 : exist.getViewCount() + 1);
            exist.setLastViewTime(now);
            footprintMapper.updateById(exist);
        } else {
            Footprint footprint = new Footprint();
            footprint.setUserId(userId);
            footprint.setProductId(productId);
            footprint.setViewCount(1);
            footprint.setLastViewTime(now);
            footprintMapper.insert(footprint);
        }
    }

    @Override
    public List<ProfileProductVO> listFootprints(Long userId) {
        List<Footprint> footprints = footprintMapper.selectList(Wrappers.<Footprint>lambdaQuery()
                .eq(Footprint::getUserId, userId)
                .orderByDesc(Footprint::getLastViewTime));
        if (footprints.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = footprints.stream().map(Footprint::getProductId).toList();
        Map<Long, Product> products = loadProducts(productIds);
        Map<Long, BigDecimal> minPrices = minPrices(productIds);
        return footprints.stream().map(f -> {
            ProfileProductVO vo = toProfileVO(products.get(f.getProductId()), minPrices.get(f.getProductId()));
            vo.setLastViewTime(f.getLastViewTime());
            vo.setViewCount(f.getViewCount());
            return vo;
        }).filter(v -> v.getProductId() != null).collect(Collectors.toList());
    }

    // ---------- helpers ----------

    private Map<Long, Product> loadProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private Map<Long, BigDecimal> minPrices(List<Long> productIds) {
        List<Sku> skus = skuMapper.selectList(Wrappers.<Sku>lambdaQuery()
                .in(Sku::getProductId, productIds)
                .eq(Sku::getStatus, 1));
        Map<Long, List<Sku>> group = skus.stream().collect(Collectors.groupingBy(Sku::getProductId));
        return group.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().map(this::effectivePrice)
                        .filter(java.util.Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(null)));
    }

    private BigDecimal effectivePrice(Sku sku) {
        if (sku == null) {
            return null;
        }
        if (sku.getIsDiscount() != null && sku.getIsDiscount() == 1 && sku.getDiscountPrice() != null) {
            return sku.getDiscountPrice();
        }
        return sku.getPrice();
    }

    private ProfileProductVO toProfileVO(Product product, BigDecimal minPrice) {
        if (product == null) {
            return new ProfileProductVO();
        }
        ProfileProductVO vo = new ProfileProductVO();
        vo.setProductId(product.getId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setCoverImg(product.getCoverImg());
        vo.setMinPrice(minPrice);
        vo.setSaleType(product.getSaleType());
        vo.setIsNearExpiry(product.getIsNearExpiry());
        return vo;
    }

    private void mustProduct(Long productId) {
        if (productMapper.selectById(productId) == null) {
            throw new BusinessException(2001, "商品不存在");
        }
    }
}