package com.mall.module.profile.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.profile.service.ProfileService;
import com.mall.module.profile.vo.ProfileProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 个人中心：收藏 / 足迹
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "我的收藏")
    @GetMapping("/favorite/list")
    public Result<List<ProfileProductVO>> favorites() {
        return Result.ok(profileService.listFavorites(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "收藏状态")
    @GetMapping("/favorite/status/{productId}")
    public Result<Map<String, Object>> status(@PathVariable("productId") Long productId) {
        return Result.ok(Map.of("favorited", profileService.isFavorite(StpUtil.getLoginIdAsLong(), productId)));
    }

    @Operation(summary = "切换收藏")
    @PostMapping("/favorite/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody @Validated ToggleDTO dto) {
        boolean favorited = profileService.toggleFavorite(StpUtil.getLoginIdAsLong(), dto.getProductId());
        return Result.ok(Map.of("favorited", favorited));
    }

    @Operation(summary = "记录浏览足迹")
    @PostMapping("/footprint")
    public Result<Void> footprint(@RequestBody @Validated ToggleDTO dto) {
        profileService.recordFootprint(StpUtil.getLoginIdAsLong(), dto.getProductId());
        return Result.ok();
    }

    @Operation(summary = "我的浏览足迹")
    @GetMapping("/footprint/list")
    public Result<List<ProfileProductVO>> footprints() {
        return Result.ok(profileService.listFootprints(StpUtil.getLoginIdAsLong()));
    }

    @Data
    public static class ToggleDTO {
        @NotNull(message = "商品不能为空")
        private Long productId;
    }
}