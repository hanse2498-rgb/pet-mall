package com.mall.module.admin.controller;

import com.mall.common.Result;
import com.mall.module.admin.entity.AdminUser;
import com.mall.module.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台认证
 */
@Tag(name = "后台认证")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserService adminUserService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginDTO dto) {
        AdminUser admin = adminUserService.login(dto.getUsername(), dto.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil_getToken());
        data.put("adminId", admin.getId());
        data.put("nickname", admin.getNickname());
        return Result.ok("登录成功", data);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        adminUserService.logout();
        return Result.ok();
    }

    @Operation(summary = "当前管理员")
    @GetMapping("/me")
    public Result<AdminUser> me() {
        return Result.ok(adminUserService.current());
    }

    private String StpUtil_getToken() {
        return cn.dev33.satoken.stp.StpUtil.getTokenValue();
    }

    @Data
    public static class LoginDTO {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}