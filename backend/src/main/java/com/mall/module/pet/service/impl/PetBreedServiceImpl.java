package com.mall.module.pet.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.module.pet.dto.RecommendItemDTO;
import com.mall.module.pet.entity.BreedRecommend;
import com.mall.module.pet.entity.PetBreed;
import com.mall.module.pet.mapper.BreedRecommendMapper;
import com.mall.module.pet.mapper.PetBreedMapper;
import com.mall.module.pet.service.PetBreedService;
import com.mall.module.pet.vo.PetBreedDetailVO;
import com.mall.module.product.mapper.ProductMapper;
import com.mall.module.product.vo.ProductCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetBreedServiceImpl implements PetBreedService {

    private final PetBreedMapper petBreedMapper;
    private final BreedRecommendMapper breedRecommendMapper;
    private final ProductMapper productMapper;

    @Override
    public List<PetBreed> list(Integer type) {
        var wrapper = Wrappers.<PetBreed>lambdaQuery().eq(PetBreed::getStatus, 1);
        if (type != null) {
            wrapper.eq(PetBreed::getType, type);
        }
        wrapper.orderByAsc(PetBreed::getSort).orderByAsc(PetBreed::getId);
        return petBreedMapper.selectList(wrapper);
    }

    @Override
    public PetBreedDetailVO detail(Long id) {
        PetBreed breed = petBreedMapper.selectById(id);
        if (breed == null) {
            throw new BusinessException("品种不存在");
        }
        PetBreedDetailVO vo = new PetBreedDetailVO();
        BeanUtils.copyProperties(breed, vo);
        vo.setRecommendProducts(buildRecommend(id));
        return vo;
    }

    @Override
    public List<BreedRecommend> recommends(Long breedId) {
        return breedRecommendMapper.selectList(Wrappers.<BreedRecommend>lambdaQuery()
                .eq(BreedRecommend::getBreedId, breedId)
                .orderByAsc(BreedRecommend::getSort));
    }

    @Override
    @Transactional
    public void saveRecommends(Long breedId, List<RecommendItemDTO> items) {
        PetBreed breed = petBreedMapper.selectById(breedId);
        if (breed == null) {
            throw new BusinessException("品种不存在");
        }
        breedRecommendMapper.delete(
                Wrappers.<BreedRecommend>lambdaQuery().eq(BreedRecommend::getBreedId, breedId));
        if (items == null || items.isEmpty()) {
            return;
        }
        int sort = 1;
        for (RecommendItemDTO it : items) {
            if (it.getProductId() == null) {
                continue;
            }
            BreedRecommend r = new BreedRecommend();
            r.setBreedId(breedId);
            r.setProductId(it.getProductId());
            r.setType(it.getType() == null ? 2 : it.getType());
            r.setSort(sort++);
            breedRecommendMapper.insert(r);
        }
    }

    /** 按手工映射取推荐商品，保持映射顺序 */
    private List<ProductCardVO> buildRecommend(Long breedId) {
        List<BreedRecommend> recs = breedRecommendMapper.selectList(
                Wrappers.<BreedRecommend>lambdaQuery()
                        .eq(BreedRecommend::getBreedId, breedId)
                        .orderByAsc(BreedRecommend::getSort));
        if (recs.isEmpty()) {
            return List.of();
        }
        List<Long> ids = recs.stream().map(BreedRecommend::getProductId).collect(Collectors.toList());
        List<ProductCardVO> cards = productMapper.selectCardsByIds(ids);
        Map<Long, ProductCardVO> map = cards.stream()
                .collect(Collectors.toMap(ProductCardVO::getId, c -> c, (a, b) -> a));
        List<ProductCardVO> ordered = new ArrayList<>();
        for (Long pid : ids) {
            ProductCardVO c = map.get(pid);
            if (c != null) {
                ordered.add(c);
            }
        }
        return ordered;
    }

    @Override
    public PageResult<PetBreed> adminPage(Integer type, String keyword, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<PetBreed>lambdaQuery();
        if (type != null) {
            wrapper.eq(PetBreed::getType, type);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(PetBreed::getName, keyword).or().like(PetBreed::getEnName, keyword));
        }
        wrapper.orderByAsc(PetBreed::getType).orderByAsc(PetBreed::getSort).orderByAsc(PetBreed::getId);
        IPage<PetBreed> page = petBreedMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public PetBreed create(PetBreed breed) {
        if (breed.getType() == null) {
            throw new BusinessException("请选择类型");
        }
        if (breed.getName() == null || breed.getName().isBlank()) {
            throw new BusinessException("请填写品种名");
        }
        if (breed.getStatus() == null) {
            breed.setStatus(1);
        }
        if (breed.getSort() == null) {
            breed.setSort(0);
        }
        if (breed.getDifficulty() == null) {
            breed.setDifficulty(1);
        }
        breed.setId(null);
        petBreedMapper.insert(breed);
        return breed;
    }

    @Override
    public PetBreed update(PetBreed breed) {
        if (breed.getId() == null) {
            throw new BusinessException("缺少 id");
        }
        PetBreed exist = petBreedMapper.selectById(breed.getId());
        if (exist == null) {
            throw new BusinessException("品种不存在");
        }
        petBreedMapper.updateById(breed);
        return petBreedMapper.selectById(breed.getId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        PetBreed breed = petBreedMapper.selectById(id);
        if (breed == null) {
            throw new BusinessException("品种不存在");
        }
        breed.setStatus(status);
        petBreedMapper.updateById(breed);
    }

    @Override
    public void delete(Long id) {
        petBreedMapper.deleteById(id);
    }
}
