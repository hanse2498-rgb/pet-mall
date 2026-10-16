package com.mall.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 角色/权限提供者：区分普通用户与管理员
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Object type = StpUtil.getSessionByLoginId(loginId).get("accountType");
            if ("admin".equals(type)) {
                return List.of("admin");
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }
}