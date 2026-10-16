package com.mall.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 本地上传配置
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    /** 本地存储目录 */
    private String dir = "./upload";

    /** 商品图片目录（上传的商品图会直接存到这里，前端经 /products/** 访问） */
    private String productDir = "../frontend/public/products";

    /** 宠物（品种）图片目录（上传的品种图案存到这里，前端经 /breeds/** 访问） */
    private String breedDir = "../frontend/public/breeds";

    /** 单文件大小上限（MB） */
    private long maxSizeMb = 5;

    /** 允许的后缀 */
    private String[] allowExt = {"jpg", "jpeg", "png", "webp", "gif"};

    @PostConstruct
    public void init() {
        // 归一化为绝对路径，避免 transferTo 解析到 Tomcat 临时目录
        this.dir = Paths.get(dir).toAbsolutePath().toString();
        try {
            Files.createDirectories(Paths.get(dir));
            log.info("上传目录就绪: {}", dir);
        } catch (Exception e) {
            log.warn("创建上传目录失败: {}", e.getMessage());
        }
        // 商品图片目录
        this.productDir = Paths.get(productDir).toAbsolutePath().toString();
        try {
            Files.createDirectories(Paths.get(productDir));
            log.info("商品图片目录就绪: {}", productDir);
        } catch (Exception e) {
            log.warn("创建商品图片目录失败: {}", e.getMessage());
        }
        // 宠物（品种）图片目录
        this.breedDir = Paths.get(breedDir).toAbsolutePath().toString();
        try {
            Files.createDirectories(Paths.get(breedDir));
            log.info("宠物图片目录就绪: {}", breedDir);
        } catch (Exception e) {
            log.warn("创建宠物图片目录失败: {}", e.getMessage());
        }
    }
}