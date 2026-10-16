package com.mall.module.admin.service;

import com.mall.module.admin.entity.AdminUser;

/**
 * 后台用户服务
 */
public interface AdminUserService {

    /** 登录成功返回管理员（内部调 Sa-Token 登录并标记管理员角色） */
    AdminUser login(String username, String password);

    /** 退出 */
    void logout();

    /** 当前管理员 */
    AdminUser current();
}