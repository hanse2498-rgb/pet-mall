package com.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.module.product.entity.Category;
import com.mall.module.product.mapper.CategoryMapper;
import com.mall.module.product.service.CategoryService;
import com.mall.module.product.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> tree() {
        List<Category> all = this.list(Wrappers.<Category>lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));

        List<CategoryVO> roots = new ArrayList<>();
        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        for (Category c : all) {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            voMap.put(c.getId(), vo);
        }
        for (Category c : all) {
            CategoryVO vo = voMap.get(c.getId());
            if (c.getParentId() != null && c.getParentId() != 0 && voMap.containsKey(c.getParentId())) {
                voMap.get(c.getParentId()).getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        return roots;
    }
}