package com.mall.module.rank.service.impl;

import com.mall.module.rank.mapper.RankMapper;
import com.mall.module.rank.service.RankService;
import com.mall.module.rank.vo.CategoryRankVO;
import com.mall.module.rank.vo.RankItemVO;
import com.mall.module.rank.vo.RankRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private static final int TOP_N = 3;

    private final RankMapper rankMapper;

    @Override
    public List<CategoryRankVO> topByCategory() {
        List<RankRowVO> rows = rankMapper.selectRankRows();

        // 按分类顺序分组，每个分类取前 TOP_N（行已按 分类sort + 销量降序 排好）
        LinkedHashMap<Long, CategoryRankVO> grouped = new LinkedHashMap<>();
        for (RankRowVO row : rows) {
            CategoryRankVO category = grouped.computeIfAbsent(row.getCategoryId(), id -> {
                CategoryRankVO vo = new CategoryRankVO();
                vo.setCategoryId(row.getCategoryId());
                vo.setCategoryName(row.getCategoryName());
                vo.setItems(new ArrayList<>());
                return vo;
            });
            if (category.getItems().size() >= TOP_N) {
                continue;
            }
            RankItemVO item = new RankItemVO();
            item.setRank(category.getItems().size() + 1);
            item.setProductId(row.getProductId());
            item.setName(row.getName());
            item.setCoverImg(row.getCoverImg());
            item.setPrice(row.getMinPrice());
            item.setSales7d(row.getSales7d());
            category.getItems().add(item);
        }
        return new ArrayList<>(grouped.values());
    }
}
