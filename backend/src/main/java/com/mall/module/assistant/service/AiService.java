package com.mall.module.assistant.service;

import com.mall.module.assistant.entity.ChatMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * 宠物问答助手（仅限宠物话题）。
 * <p>默认实现为本地知识库；配置 ai.provider=openai 时切换到外部大模型（真流式）。
 */
public interface AiService {

    /**
     * 流式生成回答：内容分段通过 onDelta 回调逐段返回，同时写入会话历史。
     *
     * @param userId   当前用户
     * @param question 用户提问
     * @param onDelta  内容片段回调（可能被多次调用）
     */
    void replyStream(Long userId, String question, Consumer<String> onDelta);

    /** 加载用户历史 */
    List<ChatMessage> history(Long userId);

    /** 清空用户历史 */
    void clear(Long userId);
}
