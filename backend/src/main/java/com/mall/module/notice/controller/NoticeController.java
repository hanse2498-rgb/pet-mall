package com.mall.module.notice.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.module.notice.entity.Notice;
import com.mall.module.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 站内通知（需登录）
 */
@Tag(name = "站内通知")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "我的消息")
    @GetMapping("/list")
    public Result<PageResult<Notice>> list(@RequestParam(required = false) Integer type,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(noticeService.page(StpUtil.getLoginIdAsLong(), type, pageNum, pageSize));
    }

    @Operation(summary = "未读数")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        long c = noticeService.unreadCount(StpUtil.getLoginIdAsLong());
        return Result.ok(Map.of("count", c));
    }

    @Operation(summary = "单条已读")
    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable("id") Long id) {
        noticeService.read(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "全部已读")
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
