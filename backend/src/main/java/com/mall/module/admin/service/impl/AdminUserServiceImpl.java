package com.mall.module.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mall.common.BusinessException;
import com.mall.module.admin.entity.AdminUser;
import com.mall.module.admin.mapper.AdminUserMapper;
import com.mall.module.admin.service.AdminUserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    /** 默认管理员账号 */
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    @PostConstruct
    public void seedDefaultAdmin() {
        Long count = adminUserMapper.selectCount(Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, DEFAULT_ADMIN_USERNAME));
        if (count == null || count == 0) {
            AdminUser admin = new AdminUser();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPasswordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setNickname("超级管理员");
            admin.setStatus(1);
            adminUserMapper.insert(admin);
            log.info("已初始化默认管理员：{} / {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        }
    }

    @Override
    public AdminUser login(String username, String password) {
        AdminUser admin = adminUserMapper.selectOne(Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, username));
        if (admin == null || admin.getPasswordHash() == null
                || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BusinessException(4001, "账号或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(4002, "账号已被禁用");
        }
        // Sa-Token 登录并标记角色为 admin
        StpUtil.login(admin.getId());
        StpUtil.getSession().set("accountType", "admin");
        admin.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        return admin;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public AdminUser current() {
        Long id = StpUtil.getLoginIdAsLong();
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(401, "管理员不存在");
        }
        admin.setPasswordHash(null);
        return admin;
    }
}