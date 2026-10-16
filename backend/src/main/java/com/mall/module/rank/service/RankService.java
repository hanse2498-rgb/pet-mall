package com.mall.module.rank.service;

import com.mall.module.rank.vo.CategoryRankVO;

import java.util.List;

public interface RankService {

    /** 各分类近 7 天销量 Top3（按分类顺序铺开） */
    List<CategoryRankVO> topByCategory();
}
