package com.mall.module.profile.service;

import com.mall.module.profile.vo.ProfileProductVO;

import java.util.List;

public interface ProfileService {

    /** 我的收藏 */
    List<ProfileProductVO> listFavorites(Long userId);

    /** 切换收藏，返回收藏后状态 */
    boolean toggleFavorite(Long userId, Long productId);

    /** 是否已收藏 */
    boolean isFavorite(Long userId, Long productId);

    /** 记录浏览足迹（重复浏览更新时间和次数） */
    void recordFootprint(Long userId, Long productId);

    /** 我的浏览足迹 */
    List<ProfileProductVO> listFootprints(Long userId);
}