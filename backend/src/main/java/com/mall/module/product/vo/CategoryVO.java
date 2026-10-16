package com.mall.module.product.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private List<CategoryVO> children = new ArrayList<>();
}