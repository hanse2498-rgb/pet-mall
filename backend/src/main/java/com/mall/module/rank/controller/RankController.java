package com.mall.module.rank.controller;

import com.mall.common.Result;
import com.mall.module.rank.service.RankService;
import com.mall.module.rank.vo.CategoryRankVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 好用排行（前台公开）
 */
@Tag(name = "好用排行")
@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @Operation(summary = "各分类近7天销量 Top3")
    @GetMapping("/top")
    public Result<List<CategoryRankVO>> top() {
        return Result.ok(rankService.topByCategory());
    }
}
