package com.mall.module.pet.service;

import com.mall.common.PageResult;
import com.mall.module.pet.dto.RecommendItemDTO;
import com.mall.module.pet.entity.BreedRecommend;
import com.mall.module.pet.entity.PetBreed;
import com.mall.module.pet.vo.PetBreedDetailVO;

import java.util.List;

public interface PetBreedService {

    /** 按类型列出上架品种（1猫 2狗） */
    List<PetBreed> list(Integer type);

    /** 品种详情（含推荐商品） */
    PetBreedDetailVO detail(Long id);

    /** 某品种的推荐映射（后台用，原始行） */
    List<BreedRecommend> recommends(Long breedId);

    /** 覆盖保存某品种的推荐映射 */
    void saveRecommends(Long breedId, List<RecommendItemDTO> items);

    // ---------- 后台 ----------

    /** 后台分页 */
    PageResult<PetBreed> adminPage(Integer type, String keyword, Integer pageNum, Integer pageSize);

    /** 新增 */
    PetBreed create(PetBreed breed);

    /** 编辑 */
    PetBreed update(PetBreed breed);

    /** 上下架 */
    void updateStatus(Long id, Integer status);

    /** 删除 */
    void delete(Long id);
}
