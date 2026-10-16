package com.mall.module.member.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.BusinessException;
import com.mall.module.member.entity.User;
import com.mall.module.member.mapper.UserMapper;
import com.mall.module.member.service.SmsCodeService;
import com.mall.module.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;

    /** 密码登录失败次数限制（内存版，开发用） */
    private static final int MAX_FAIL = 5;
    private final Map<String, FailureEntry> failRecords = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public User register(String phone, String code, String password, String nickname) {
        if (findByPhone(phone) != null) {
            throw new BusinessException(1007, "该手机号已注册，请直接登录");
        }
        if (!smsCodeService.verify(phone, code)) {
            throw new BusinessException(1008, "验证码错误或已过期");
        }
        User user = new User();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank()
                ? "宠物家长-" + phone.substring(phone.length() - 4) : nickname.trim());
        user.setStatus(1);
        this.save(user);
        return user;
    }

    @Override
    public User loginByCode(String phone, String code) {
        if (!smsCodeService.verify(phone, code)) {
            throw new BusinessException(1008, "验证码错误或已过期");
        }
        return loginSuccess(phone);
    }

    @Override
    public User loginByPassword(String phone, String password) {
        FailureEntry entry = failRecords.computeIfAbsent(phone, k -> new FailureEntry());
        synchronized (entry) {
            if (entry.count.get() >= MAX_FAIL
                    && entry.lockUntil != null
                    && entry.lockUntil.isAfter(LocalDateTime.now())) {
                long minutes = entry.lockUntil.until(LocalDateTime.now().plusMinutes(15), java.time.temporal.ChronoUnit.MINUTES);
                throw new BusinessException(1009, "尝试次数过多，请 " + Math.max(0, minutes) + " 分钟后重试");
            }
        }
        User user = findByPhone(phone);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            recordFail(phone);
            throw new BusinessException(1002, "手机号或密码错误");
        }
        failRecords.remove(phone);
        return loginSuccess(phone);
    }

    @Override
    @Transactional
    public void resetPassword(String phone, String code, String newPassword) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new BusinessException(1001, "该手机号未注册");
        }
        if (!smsCodeService.verify(phone, code)) {
            throw new BusinessException(1008, "验证码错误或已过期");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public User findByPhone(String phone) {
        return this.getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
    }

    // ---------- helpers ----------

    private User loginSuccess(String phone) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new BusinessException(1001, "该手机号未注册，请先注册");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(1010, "账号已被禁用，如有疑问请联系客服");
        }
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
        return user;
    }

    private void recordFail(String phone) {
        failRecords.computeIfAbsent(phone, k -> new FailureEntry()).count.incrementAndGet();
        FailureEntry entry = failRecords.get(phone);
        if (entry.count.get() >= MAX_FAIL) {
            entry.lockUntil = LocalDateTime.now().plusMinutes(15);
        }
    }

    static class FailureEntry {
        final AtomicInteger count = new AtomicInteger(0);
        volatile LocalDateTime lockUntil;
    }
}