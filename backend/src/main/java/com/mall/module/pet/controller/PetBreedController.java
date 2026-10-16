package com.mall.module.pet.controller;

import com.mall.common.Result;
import com.mall.module.pet.entity.PetBreed;
import com.mall.module.pet.service.PetBreedService;
import com.mall.module.pet.vo.PetBreedDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物档案（犬猫品种图鉴，前台公开）
 */
@Tag(name = "宠物档案")
@RestController
@RequestMapping("/api/pet/breed")
@RequiredArgsConstructor
public class PetBreedController {

    private final PetBreedService petBreedService;

    @Operation(summary = "品种列表（type: 1猫 2狗）")
    @GetMapping
    public Result<List<PetBreed>> list(@RequestParam(required = false) Integer type) {
        return Result.ok(petBreedService.list(type));
    }

    @Operation(summary = "品种详情")
    @GetMapping("/{id}")
    public Result<PetBreedDetailVO> detail(@PathVariable("id") Long id) {
        return Result.ok(petBreedService.detail(id));
    }
}
