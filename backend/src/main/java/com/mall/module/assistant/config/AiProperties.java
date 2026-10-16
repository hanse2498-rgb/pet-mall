package com.mall.module.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 问答助手配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** local：本地知识库 | openai：外部 OpenAI 兼容大模型 */
    private String provider = "local";

    private OpenAi openai = new OpenAi();

    public boolean isOpenaiEnabled() {
        return "openai".equalsIgnoreCase(provider)
                && openai.getApiKey() != null && !openai.getApiKey().isBlank();
    }

    @Data
    public static class OpenAi {
        /** OpenAI 兼容端点，例如 https://api.openai.com/v1 或中转地址 */
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        /** 最大回复 token */
        private Integer maxTokens = 1000;
    }
}