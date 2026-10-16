package com.mall.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Sa-Token 拦截 + CORS 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${upload.dir:./upload}")
    private String uploadDir;

    /** 无需登录即可访问的路由 */
    private static final String[] EXCLUDE_PATHS = {
            "/api/auth/**",
            "/api/admin/auth/login",
            "/api/product/**",
            "/api/rank/**",
            "/api/pet/**",
            "/api/review/product/**",
            "/api/community/**",
            "/api/seckill/**",
            "/files/**",
            "/api/ping",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/**")
                    .notMatch(EXCLUDE_PATHS)
                    .check(r -> StpUtil.checkLogin());
            // 后台接口仅管理员可访问（登录接口除外）
            SaRouter.match("/api/admin/**")
                    .notMatch("/api/admin/auth/login")
                    .check(r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地图片静态访问：/files/** -> upload 目录
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath() + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}