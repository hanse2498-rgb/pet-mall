package com.mall.module.member.service;

import com.mall.common.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 短信验证码服务（当前为模拟实现）
 *
 * <p>说明：真实上线时替换为短信网关推送；此处生成验证码并打印到日志，
 * 接口响应中附带 devCode 仅用于本机演示。
 *
 * 规则：
 * <ul>
 *   <li>验证码 6 位数字，有效期 5 分钟</li>
 *   <li>同一手机号 60 秒内不可重复发送</li>
 *   <li>同一手机号每天最多 10 条</li>
 *   <li>验证码一次性使用，校验后作废</li>
 * </ul>
 */
@Slf4j
@Service
public class SmsCodeService {

    private static final Duration EXPIRE = Duration.ofMinutes(5);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final int DAILY_LIMIT = 10;

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    @Data
    static class CodeEntry {
        String code;
        LocalDateTime createAt;
        LocalDateTime lastSendAt;
        LocalDate day;
        int dailyCount;
        boolean consumed;
    }

    /**
     * 发送验证码，返回生成的值
     */
    public String send(String phone) {
        CodeEntry entry = store.computeIfAbsent(phone, k -> new CodeEntry());
        synchronized (entry) {
            LocalDateTime now = LocalDateTime.now();
            if (entry.getLastSendAt() != null
                    && Duration.between(entry.getLastSendAt(), now).compareTo(RESEND_INTERVAL) < 0) {
                long remain = RESEND_INTERVAL.toSeconds()
                        - Duration.between(entry.getLastSendAt(), now).toSeconds();
                throw new BusinessException(1005, "发送太频繁，请 " + remain + " 秒后重试");
            }
            // 每天计数
            if (!now.toLocalDate().equals(entry.getDay())) {
                entry.setDay(now.toLocalDate());
                entry.setDailyCount(0);
            }
            if (entry.getDailyCount() >= DAILY_LIMIT) {
                throw new BusinessException(1006, "今日验证码发送次数已达上限");
            }
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            entry.setCode(code);
            entry.setCreateAt(now);
            entry.setLastSendAt(now);
            entry.setConsumed(false);
            entry.setDailyCount(entry.getDailyCount() + 1);
            log.info("[模拟短信] 手机号 {} 验证码 {}", phone, code);
            return code;
        }
    }

    /**
     * 校验验证码（校验通过即消费，一次性）
     */
    public boolean verify(String phone, String code) {
        CodeEntry entry = store.get(phone);
        if (entry == null) {
            return false;
        }
        synchronized (entry) {
            if (entry.isConsumed()) {
                return false;
            }
            if (Duration.between(entry.getCreateAt(), LocalDateTime.now()).compareTo(EXPIRE) > 0) {
                return false;
            }
            if (!entry.getCode().equals(code)) {
                return false;
            }
            entry.setConsumed(true);
            return true;
        }
    }
}