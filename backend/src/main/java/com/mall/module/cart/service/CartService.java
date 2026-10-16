package com.mall.module.cart.service;

import com.mall.module.cart.vo.CartItemVO;

import java.util.List;

public interface CartService {

    /** 加入购物车（已存在则累加数量） */
    CartItemVO add(Long userId, Long skuId, Integer quantity);

    /** 购物车列表 */
    List<CartItemVO> list(Long userId);

    /** 勾选的购物车项（结算用） */
    List<CartItemVO> listSelected(Long userId);

    /** 更新数量或勾选状态 */
    void update(Long userId, Long cartId, Integer quantity, Integer selected);

    /** 删除 */
    void remove(Long userId, Long cartId);

    /** 购物车商品总件数 */
    long count(Long userId);

    /** 清空已购买（下单成功后移除对应项） */
    void removeBySkuIds(Long userId, List<Long> skuIds);
}