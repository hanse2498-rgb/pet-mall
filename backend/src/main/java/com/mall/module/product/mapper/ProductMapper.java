package com.mall.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.module.product.dto.ProductQuery;
import com.mall.module.product.entity.Product;
import com.mall.module.product.vo.ProductCardVO;
import com.mall.module.product.vo.ProductDetailVO;
import com.mall.module.product.vo.ProductSuggestVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    /** 分页查询商品卡片（含最低价/品牌/分类） */
    IPage<ProductCardVO> selectCardPage(IPage<ProductCardVO> page, @Param("q") ProductQuery query);

    /** 商品详情基础信息（含品牌名/分类名） */
    ProductDetailVO selectDetail(@Param("id") Long id);

    /** 搜索联想：在售商品，按相关性+销量取前 N 条 */
    List<ProductSuggestVO> selectSuggest(@Param("kw") String keyword, @Param("limit") int limit);

    /** 搜索命中的商品总数 */
    long countSuggest(@Param("kw") String keyword);

    /** 按 id 批量取商品卡片（在售，保序由调用方处理） */
    List<ProductCardVO> selectCardsByIds(@Param("ids") List<Long> ids);
}