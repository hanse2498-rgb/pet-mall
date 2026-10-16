package com.mall.module.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.module.member.entity.UserAddress;

public interface UserAddressService extends IService<UserAddress> {

    /** 新增（首个地址自动设为默认） */
    UserAddress add(Long userId, UserAddress address);

    /** 更新（校验归属） */
    void update(Long userId, UserAddress address);

    /** 删除（默认地址被删除后，设置最新一条为默认） */
    void deleteAddress(Long userId, Long id);

    /** 设为默认 */
    void setDefault(Long userId, Long id);

    /** 获取可用地址（默认在前） */
    java.util.List<UserAddress> listByUser(Long userId);
}