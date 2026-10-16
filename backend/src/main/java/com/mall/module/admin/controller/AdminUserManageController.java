package com.mall.module.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BusinessException;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.member.entity.User;
import com.mall.module.member.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理
 */
@Tag(name = "后台用户")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserManageController {

    private final UserService userService;

    @Operation(summary = "用户分页（手机号/昵称搜索）")
    @GetMapping
    public Result<PageResult<User>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        var wrapper = Wrappers.<User>lambdaQuery();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(User::getPhone, keyword).or().like(User::getNickname, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(u -> u.setPasswordHash(null));
        return Result.ok(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable("id") Long id, @RequestBody StatusDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        user.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        userService.updateById(user);
        return Result.ok();
    }

    @Data
    public static class StatusDTO {
        private Integer status;
    }
}