package com.mall.module.pet.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.pet.dto.RecommendItemDTO;
import com.mall.module.pet.entity.BreedRecommend;
import com.mall.module.pet.entity.PetBreed;
import com.mall.module.pet.service.PetBreedService;
import com.mall.module.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 宠物档案管理（后台）
 */
@Tag(name = "后台宠物档案")
@RestController
@RequestMapping("/api/admin/pet/breed")
@RequiredArgsConstructor
public class AdminPetBreedController {

    private final PetBreedService petBreedService;
    private final UploadService uploadService;

    @Operation(summary = "上传品种图案（保存到宠物图片文件夹）")
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Long breedId) {
        String url = uploadService.saveBreedImage(file, breedId);
        return Result.ok(Map.of("url", url));
    }

    @Operation(summary = "品种列表")
    @GetMapping
    public Result<PageResult<PetBreed>> page(@RequestParam(required = false) Integer type,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(petBreedService.adminPage(type, keyword, pageNum, pageSize));
    }

    @Operation(summary = "新增品种")
    @PostMapping
    public Result<PetBreed> create(@RequestBody PetBreed breed) {
        return Result.ok(petBreedService.create(breed));
    }

    @Operation(summary = "编辑品种")
    @PutMapping
    public Result<PetBreed> update(@RequestBody PetBreed breed) {
        return Result.ok(petBreedService.update(breed));
    }

    @Operation(summary = "上下架")
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable("id") Long id, @RequestBody StatusDTO dto) {
        petBreedService.updateStatus(id, dto.getStatus());
        return Result.ok();
    }

    @Operation(summary = "删除品种")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        petBreedService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查询品种推荐映射")
    @GetMapping("/{id}/recommend")
    public Result<List<BreedRecommend>> recommend(@PathVariable("id") Long id) {
        return Result.ok(petBreedService.recommends(id));
    }

    @Operation(summary = "保存品种推荐映射")
    @PutMapping("/{id}/recommend")
    public Result<Void> saveRecommend(@PathVariable("id") Long id, @RequestBody List<RecommendItemDTO> items) {
        petBreedService.saveRecommends(id, items);
        return Result.ok();
    }

    @Data
    public static class StatusDTO {
        private Integer status;
    }
}
