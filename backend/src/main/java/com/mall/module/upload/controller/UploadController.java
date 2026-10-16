package com.mall.module.upload.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片上传（开发版：本地磁盘存储）
 */
@Tag(name = "图片上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        StpUtil.checkLogin();
        String url = uploadService.saveImage(file);
        return Result.ok(Map.of("url", url, "size", file.getSize()));
    }
}