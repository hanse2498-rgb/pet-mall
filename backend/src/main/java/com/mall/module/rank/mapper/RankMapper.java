package com.mall.module.rank.mapper;

import com.mall.module.rank.vo.RankRowVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RankMapper {

    /** 近 7 天有销量的在售商品（按分类、近7天销量降序） */
    List<RankRowVO> selectRankRows();
}
