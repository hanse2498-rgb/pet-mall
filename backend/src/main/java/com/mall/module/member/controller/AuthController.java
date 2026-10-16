package com.mall.module.member.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.member.entity.User;
import com.mall.module.member.service.SmsCodeService;
import com.mall.module.member.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：注册 / 验证码登录 / 密码登录 / 忘记密码
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;
    private final SmsCodeService smsCodeService;

    @Operation(summary = "发送短信验证码（模拟，devCode 仅演示用）")
    @PostMapping("/sms-code")
    public Result<Map<String, Object>> sendSmsCode(@RequestBody @Validated SendSmsDTO dto) {
        String code = smsCodeService.send(dto.getPhone());
        Map<String, Object> data = new HashMap<>();
        data.put("devCode", code); // 仅本机/测试环境返回，上线接真实短信网关后移除
        data.put("expireSeconds", 300);
        data.put("resendSeconds", 60);
        return Result.ok("验证码已发送", data);
    }

    @Operation(summary = "注册（手机号+验证码+密码）")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Validated RegisterDTO dto) {
        User user = userService.register(dto.getPhone(), dto.getCode(), dto.getPassword(), dto.getNickname());
        return tokenResult(user);
    }

    @Operation(summary = "验证码登录")
    @PostMapping("/login/code")
    public Result<Map<String, Object>> loginByCode(@RequestBody @Validated CodeLoginDTO dto) {
        User user = userService.loginByCode(dto.getPhone(), dto.getCode());
        return tokenResult(user);
    }

    @Operation(summary = "密码登录")
    @PostMapping("/login/password")
    public Result<Map<String, Object>> loginByPassword(@RequestBody @Validated PasswordLoginDTO dto) {
        User user = userService.loginByPassword(dto.getPhone(), dto.getPassword());
        return tokenResult(user);
    }

    @Operation(summary = "忘记密码（重置）")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody @Validated ForgotDTO dto) {
        userService.resetPassword(dto.getPhone(), dto.getCode(), dto.getNewPassword());
        return Result.ok();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<User> me() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userService.getById(userId));
    }

    private Result<Map<String, Object>> tokenResult(User user) {
        StpUtil.login(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        return Result.ok("登录成功", data);
    }

    // ---------- DTO ----------

    @Data
    public static class SendSmsDTO {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    @Data
    public static class RegisterDTO {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
        private String code;
        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&_.]{8,20}$",
                message = "密码需8-20位，且包含字母和数字")
        private String password;
        private String nickname;
    }

    @Data
    public static class CodeLoginDTO {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
        private String code;
    }

    @Data
    public static class PasswordLoginDTO {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class ForgotDTO {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
        private String code;
        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&_.]{8,20}$",
                message = "密码需8-20位，且包含字母和数字")
        private String newPassword;
    }
}