package com.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / OpenAPI 配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mallOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("宠物商城 API")
                .description("宠物用品/食品商城 前后端分离接口文档")
                .version("0.1.0"));
    }
}