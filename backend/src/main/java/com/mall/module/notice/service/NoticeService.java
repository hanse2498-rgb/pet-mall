package com.mall.module.notice.service;

import com.mall.common.PageResult;
import com.mall.module.notice.entity.Notice;

public interface NoticeService {

    /** 推送一条站内消息 */
    void send(Long userId, Integer type, String title, String content, Long bizId);

    /** 我的消息分页（可筛选类型） */
    PageResult<Notice> page(Long userId, Integer type, Integer pageNum, Integer pageSize);

    /** 未读数 */
    long unreadCount(Long userId);

    /** 单条已读 */
    void read(Long userId, Long id);

    /** 全部已读 */
    void readAll(Long userId);
}
