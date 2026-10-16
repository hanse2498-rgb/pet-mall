package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.module.product.entity.Brand;
import com.mall.module.product.mapper.BrandMapper;
import com.mall.module.product.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Override
    public List<Brand> listActive() {
        return this.list(Wrappers.<Brand>lambdaQuery()
                .eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getId));
    }
}