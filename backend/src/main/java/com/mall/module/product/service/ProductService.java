package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.PageResult;
import com.mall.module.product.dto.ProductQuery;
import com.mall.module.product.entity.Product;
import com.mall.module.product.vo.ProductCardVO;
import com.mall.module.product.vo.ProductDetailVO;
import com.mall.module.product.vo.SuggestResultVO;

public interface ProductService extends IService<Product> {

    /** 分页查询商品卡片 */
    PageResult<ProductCardVO> pageCards(ProductQuery query);

    /** 商品详情 */
    ProductDetailVO detail(Long id);

    /** 临期专区 */
    PageResult<ProductCardVO> nearExpiry(ProductQuery query);

    /** 搜索联想（在售商品） */
    SuggestResultVO suggest(String keyword);

    /** SKU 直购信息 */
    com.mall.module.product.vo.SkuInfoVO skuInfo(Long skuId);
}