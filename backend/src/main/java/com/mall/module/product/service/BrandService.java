package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.module.product.entity.Brand;

import java.util.List;

public interface BrandService extends IService<Brand> {

    /** 启用的品牌列表（按排序） */
    List<Brand> listActive();
}