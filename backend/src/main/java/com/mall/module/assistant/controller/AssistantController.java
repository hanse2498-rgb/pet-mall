package com.mall.module.assistant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.mall.common.Result;
import com.mall.module.assistant.entity.ChatMessage;
import com.mall.module.assistant.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 问答助手（需登录）
 */
@Tag(name = "问答助手")
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AiService aiService;

    @Operation(summary = "问答（SSE 流式返回）")
    @GetMapping("/chat")
    public SseEmitter chat(@RequestParam("question") String question) {
        Long userId = StpUtil.getLoginIdAsLong();
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                aiService.replyStream(userId, question, delta -> {
                    try {
                        emitter.send(SseEmitter.event().data(delta));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("[DONE]"));
                } catch (IOException ignore) {
                    // 客户端可能已断开
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Operation(summary = "历史消息")
    @GetMapping("/history")
    public Result<List<ChatMessage>> history() {
        return Result.ok(aiService.history(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "清空历史")
    @DeleteMapping("/history")
    public Result<Void> clear() {
        aiService.clear(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
