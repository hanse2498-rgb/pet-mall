package com.mall.module.notice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.PageResult;
import com.mall.module.notice.entity.Notice;
import com.mall.module.notice.mapper.NoticeMapper;
import com.mall.module.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public void send(Long userId, Integer type, String title, String content, Long bizId) {
        if (userId == null || title == null) {
            return;
        }
        Notice n = new Notice();
        n.setUserId(userId);
        n.setType(type == null ? 1 : type);
        n.setTitle(title);
        n.setContent(content);
        n.setBizId(bizId);
        n.setIsRead(0);
        this.save(n);
    }

    @Override
    public PageResult<Notice> page(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        var wrapper = Wrappers.<Notice>lambdaQuery().eq(Notice::getUserId, userId);
        if (type != null) {
            wrapper.eq(Notice::getType, type);
        }
        wrapper.orderByDesc(Notice::getCreateTime).orderByDesc(Notice::getId);
        IPage<Notice> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public long unreadCount(Long userId) {
        return this.count(Wrappers.<Notice>lambdaQuery()
                .eq(Notice::getUserId, userId)
                .eq(Notice::getIsRead, 0));
    }

    @Override
    public void read(Long userId, Long id) {
        Notice n = this.getById(id);
        if (n == null || !n.getUserId().equals(userId)) {
            return;
        }
        if (n.getIsRead() == null || n.getIsRead() == 0) {
            n.setIsRead(1);
            this.updateById(n);
        }
    }

    @Override
    public void readAll(Long userId) {
        this.update(Wrappers.<Notice>lambdaUpdate()
                .eq(Notice::getUserId, userId)
                .eq(Notice::getIsRead, 0)
                .set(Notice::getIsRead, 1));
    }
}
