package com.mall.module.member.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.BusinessException;
import com.mall.module.member.entity.UserAddress;
import com.mall.module.member.mapper.UserAddressMapper;
import com.mall.module.member.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

    @Override
    @Transactional
    public UserAddress add(Long userId, UserAddress address) {
        address.setId(null);
        address.setUserId(userId);
        long count = this.count(Wrappers.<UserAddress>lambdaQuery().eq(UserAddress::getUserId, userId));
        if (count == 0) {
            address.setIsDefault(1);
        }
        this.save(address);
        return address;
    }

    @Override
    @Transactional
    public void update(Long userId, UserAddress address) {
        UserAddress exist = mustOwn(userId, address.getId());
        address.setUserId(userId);
        address.setCreateTime(exist.getCreateTime());
        this.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long id) {
        mustOwn(userId, id);
        this.removeById(id);
        boolean anyDefault = this.count(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)) > 0;
        if (!anyDefault) {
            UserAddress latest = this.getOne(Wrappers.<UserAddress>lambdaQuery()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getCreateTime)
                    .last("limit 1"));
            if (latest != null) {
                latest.setIsDefault(1);
                this.updateById(latest);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long id) {
        mustOwn(userId, id);
        this.update(Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
        this.update(Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getIsDefault, 1));
    }

    @Override
    public List<UserAddress> listByUser(Long userId) {
        return this.list(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime));
    }

    private UserAddress mustOwn(Long userId, Long id) {
        UserAddress exist = this.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new BusinessException(1004, "地址不存在");
        }
        return exist;
    }
}