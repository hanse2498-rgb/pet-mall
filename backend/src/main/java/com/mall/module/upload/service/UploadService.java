package com.mall.module.upload.service;

import com.mall.common.BusinessException;
import com.mall.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadProperties properties;

    /**
     * 保存图片，返回可访问的相对 URL（以 /files 开头）
     */
    public String saveImage(MultipartFile file) {
        String ext = validate(file);
        String contentPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(properties.getDir(), contentPath);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return "/files/" + contentPath + "/" + filename;
        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new BusinessException(500, "图片保存失败");
        }
    }

    /**
     * 保存商品图片，直接写入「商品图片文件夹」（frontend/public/products），
     * 返回可访问的相对 URL（以 /products 开头），前端静态目录即可直接访问。
     *
     * @param productId 商品 id（可为空，用于生成易识别的文件名）
     */
    public String saveProductImage(MultipartFile file, Long productId) {
        String ext = validate(file);
        String prefix = productId == null ? "p" : String.valueOf(productId);
        String filename = prefix + "-" + System.currentTimeMillis() + "." + ext;
        Path dir = Paths.get(properties.getProductDir());
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
            return "/products/" + filename;
        } catch (Exception e) {
            log.error("保存商品图片失败", e);
            throw new BusinessException(500, "图片保存失败");
        }
    }

    /**
     * 保存宠物（品种）图案，直接写入「宠物图片文件夹」（frontend/public/breeds），
     * 返回可访问的相对 URL（以 /breeds 开头）。
     *
     * @param breedId 品种 id（可为空，用于生成易识别的文件名）
     */
    public String saveBreedImage(MultipartFile file, Long breedId) {
        String ext = validate(file);
        String prefix = breedId == null ? "b" : String.valueOf(breedId);
        String filename = prefix + "-" + System.currentTimeMillis() + "." + ext;
        Path dir = Paths.get(properties.getBreedDir());
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
            return "/breeds/" + filename;
        } catch (Exception e) {
            log.error("保存宠物图片失败", e);
            throw new BusinessException(500, "图片保存失败");
        }
    }

    /** 校验并返回小写后缀 */
    private String validate(MultipartFile file) {        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        long maxBytes = properties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(400, "图片不能超过 " + properties.getMaxSizeMb() + "MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extOf(original);
        if (!Arrays.asList(properties.getAllowExt()).contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg/png/webp/gif 图片");
        }
        return ext;
    }

    private String extOf(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}