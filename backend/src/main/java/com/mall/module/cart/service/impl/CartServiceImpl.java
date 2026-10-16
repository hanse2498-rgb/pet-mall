package com.mall.module.cart.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mall.common.BusinessException;
import com.mall.module.cart.entity.Cart;
import com.mall.module.cart.mapper.CartMapper;
import com.mall.module.cart.service.CartService;
import com.mall.module.cart.vo.CartItemVO;
import com.mall.module.product.entity.Product;
import com.mall.module.product.entity.Sku;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public CartItemVO add(Long userId, Long skuId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "数量不合法");
        }
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException(2001, "商品不存在或已下架");
        }
        Cart exist = cartMapper.selectOne(Wrappers.<Cart>lambdaQuery()
                .eq(Cart::getUserId, userId).eq(Cart::getSkuId, skuId));
        if (exist != null) {
            int newQty = exist.getQuantity() + quantity;
            if (newQty > sku.getStock()) {
                throw new BusinessException(2002, "库存不足");
            }
            exist.setQuantity(newQty);
            cartMapper.updateById(exist);
            return buildVO(exist);
        }
        if (quantity > sku.getStock()) {
            throw new BusinessException(2002, "库存不足");
        }
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setSkuId(skuId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        cartMapper.insert(cart);
        return buildVO(cart);
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        return buildList(userId, false);
    }

    @Override
    public List<CartItemVO> listSelected(Long userId) {
        return buildList(userId, true);
    }

    @Override
    @Transactional
    public void update(Long userId, Long cartId, Integer quantity, Integer selected) {
        Cart cart = mustOwn(userId, cartId);
        if (quantity != null) {
            Sku sku = skuMapper.selectById(cart.getSkuId());
            if (quantity <= 0 || sku == null || quantity > sku.getStock()) {
                throw new BusinessException(2002, "库存不足");
            }
            cart.setQuantity(quantity);
        }
        if (selected != null) {
            cart.setSelected(selected);
        }
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long cartId) {
        mustOwn(userId, cartId);
        cartMapper.deleteById(cartId);
    }

    @Override
    public long count(Long userId) {
        Long sum = cartMapper.selectList(Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId, userId))
                .stream().mapToLong(Cart::getQuantity).sum();
        return sum;
    }

    @Override
    @Transactional
    public void removeBySkuIds(Long userId, List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        cartMapper.delete(Wrappers.<Cart>lambdaQuery()
                .eq(Cart::getUserId, userId)
                .in(Cart::getSkuId, skuIds));
    }

    private Cart mustOwn(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(3002, "购物车项不存在");
        }
        return cart;
    }

    private List<CartItemVO> buildList(Long userId, boolean selectedOnly) {
        var query = Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId, userId);
        if (selectedOnly) {
            query.eq(Cart::getSelected, 1);
        }
        List<Cart> carts = cartMapper.selectList(query);
        if (carts.isEmpty()) {
            return List.of();
        }
        List<Long> skuIds = carts.stream().map(Cart::getSkuId).toList();
        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, Function.identity()));
        List<Long> productIds = skuMap.values().stream().map(Sku::getProductId).distinct().toList();
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));
        return carts.stream().map(cart -> {
            Sku sku = skuMap.get(cart.getSkuId());
            Product product = sku == null ? null : productMap.get(sku.getProductId());
            CartItemVO vo = new CartItemVO();
            vo.setCartId(cart.getId());
            vo.setSkuId(cart.getSkuId());
            vo.setProductId(sku == null ? null : sku.getProductId());
            vo.setProductName(product == null ? "商品已失效" : product.getName());
            vo.setCoverImg(product == null ? null : product.getCoverImg());
            vo.setSpecName(sku == null ? null : sku.getSpecName());
            vo.setPrice(effectivePrice(sku));
            vo.setOriginalPrice(sku == null ? null : sku.getOriginalPrice());
            vo.setIsDiscount(sku == null ? 0 : sku.getIsDiscount());
            vo.setStock(sku == null ? 0 : sku.getStock());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());
            vo.setIsNearExpiry(product == null ? 0 : product.getIsNearExpiry());
            vo.setSubtotal(vo.calculateSubtotal());
            return vo;
        }).collect(Collectors.toList());
    }

    private CartItemVO buildVO(Cart cart) {
        return buildList(cart.getUserId(), false).stream()
                .filter(v -> v.getCartId().equals(cart.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(3002, "购物车项不存在"));
    }

    private BigDecimal effectivePrice(Sku sku) {
        if (sku == null) {
            return BigDecimal.ZERO;
        }
        if (sku.getIsDiscount() != null && sku.getIsDiscount() == 1 && sku.getDiscountPrice() != null) {
            return sku.getDiscountPrice();
        }
        return sku.getPrice();
    }
}