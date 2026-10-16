package com.mall.module.assistant.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.module.assistant.config.AiProperties;
import com.mall.module.assistant.entity.ChatMessage;
import com.mall.module.assistant.knowledge.KnowledgeService;
import com.mall.module.assistant.mapper.ChatMessageMapper;
import com.mall.module.assistant.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * 统一问答服务：按配置选择 本地知识库 或 外部大模型。
 */
@Service
@RequiredArgsConstructor
public class PetAssistant implements AiService {

    private final AiProperties aiProperties;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;
    private final KnowledgeService knowledgeService;

    private LocalPetAgent local;
    private OpenAiAgent openAi;

    private AiService pick() {
        if (local == null) {
            local = new LocalPetAgent(chatMessageMapper, knowledgeService);
        }
        if (aiProperties.isOpenaiEnabled()) {
            if (openAi == null) {
                openAi = new OpenAiAgent(aiProperties, chatMessageMapper, objectMapper, knowledgeService);
            }
            return openAi;
        }
        return local;
    }

    @Override
    public void replyStream(Long userId, String question, Consumer<String> onDelta) {
        pick().replyStream(userId, question, onDelta);
    }

    @Override
    public List<ChatMessage> history(Long userId) {
        return pick().history(userId);
    }

    @Override
    public void clear(Long userId) {
        pick().clear(userId);
    }
}
