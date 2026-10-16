package com.mall.module.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.module.member.entity.User;

public interface UserService extends IService<User> {

    /** 注册（手机号 + 验证码 + 密码），成功返回用户 */
    User register(String phone, String code, String password, String nickname);

    /** 验证码登录（要求已注册） */
    User loginByCode(String phone, String code);

    /** 密码登录（含失败次数限制） */
    User loginByPassword(String phone, String password);

    /** 忘记密码：验证验证码后重置密码 */
    void resetPassword(String phone, String code, String newPassword);

    /** 按手机号查找 */
    User findByPhone(String phone);
}