package com.mall.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.module.product.entity.Category;
import com.mall.module.product.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /** 分类树（两级） */
    List<CategoryVO> tree();
}