package com.mall.module.assistant.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.module.assistant.config.AiProperties;
import com.mall.module.assistant.entity.ChatMessage;
import com.mall.module.assistant.knowledge.KnowledgeDoc;
import com.mall.module.assistant.knowledge.KnowledgeService;
import com.mall.module.assistant.mapper.ChatMessageMapper;
import com.mall.module.assistant.service.AiService;
import com.mall.module.assistant.service.guard.PetTopicGuard;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 外部 OpenAI 兼容大模型助手（真流式 + 多轮上下文）。
 *
 * <p>双重话题约束：
 * 1) 请求前用 PetTopicGuard 硬性拦截，非宠物问题直接拒答（不调用外部模型，省成本且保证只答宠物）；
 * 2) system prompt 进一步限定模型只回答养宠内容并礼貌拒绝无关问题。
 */
@Slf4j
public class OpenAiAgent implements AiService {

    /** 带入对话历史的条数（约 = 轮数 × 2） */
    private static final int HISTORY_LIMIT = 10;

    private static final String SYSTEM_PROMPT =
            "你是宠物商城的宠物问答助手。你只回答与养宠相关的话题（如科学喂养、换粮、洗澡、驱虫、疫苗、绝育、宠物行为、常见健康常识、宠物用品选购等）。"
                    + "当用户询问与宠物无关的内容、闲聊其他领域或违规内容时，用一句简洁、友善的中文拒绝，并引导回宠物话题。"
                    + "回答要求：**内容详实、条理清晰**，尽量给出具体做法、关键数字（频率/剂量/时间）和注意事项；"
                    + "用中文，善用 Markdown（小标题、有序/无序列表、加粗）让内容易读；篇幅适中偏详细，避免一句话敷衍。";

    private final AiProperties props;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;
    private final KnowledgeService knowledgeService;
    private final HttpClient httpClient;

    public OpenAiAgent(AiProperties props, ChatMessageMapper chatMessageMapper,
                       ObjectMapper objectMapper, KnowledgeService knowledgeService) {
        this.props = props;
        this.chatMessageMapper = chatMessageMapper;
        this.objectMapper = objectMapper;
        this.knowledgeService = knowledgeService;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public void replyStream(Long userId, String question, Consumer<String> onDelta) {
        if (question == null || question.isBlank()) {
            emitChunks("有什么关于养宠的问题都可以问我～", onDelta);
            return;
        }
        String q = question.trim();
        save(userId, "user", q);

        // 检索知识库（宠物档案 / 商品 / 规则）
        List<KnowledgeDoc> docs = knowledgeService.retrieve(q);

        // 既非宠物话题、又未命中知识库 → 硬拦截，不调用模型
        if (!PetTopicGuard.isPetTopic(q) && docs.isEmpty()) {
            save(userId, "assistant", PetTopicGuard.REFUSE);
            emitChunks(PetTopicGuard.REFUSE, onDelta);
            return;
        }

        StringBuilder full = new StringBuilder();
        streamFromApi(userId, q, docs, full, onDelta);

        String answer;
        if (full.length() > 0) {
            answer = full.toString();
        } else {
            answer = fallback(q);
            emitChunks(answer, onDelta);
        }
        save(userId, "assistant", answer);
    }

    @Override
    public List<ChatMessage> history(Long userId) {
        return chatMessageMapper.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getCreateTime)
                .last("limit 100"));
    }

    @Override
    public void clear(Long userId) {
        chatMessageMapper.delete(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getUserId, userId));
    }

    /** 调用 OpenAI 兼容接口（stream=true），边收边回调 */
    private void streamFromApi(Long userId, String q, List<KnowledgeDoc> docs,
                               StringBuilder full, Consumer<String> onDelta) {
        try {
            String url = props.getOpenai().getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String ctx = buildContext(docs);
            if (!ctx.isEmpty()) {
                messages.add(Map.of("role", "system", "content", ctx));
            }
            messages.addAll(recentMessages(userId));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", props.getOpenai().getModel());
            body.put("temperature", 0.7);
            body.put("max_tokens", props.getOpenai().getMaxTokens() == null ? 500 : props.getOpenai().getMaxTokens());
            body.put("stream", true);
            body.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + props.getOpenai().getApiKey())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Stream<String>> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
            if (resp.statusCode() != 200) {
                log.warn("外部LLM流式返回非200: {}", resp.statusCode());
                return;
            }
            try (Stream<String> lines = resp.body()) {
                Iterator<String> it = lines.iterator();
                while (it.hasNext()) {
                    String line = it.next();
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String payload = line.substring(5).trim();
                    if (payload.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(payload)) {
                        break;
                    }
                    JsonNode delta = objectMapper.readTree(payload)
                            .path("choices").path(0).path("delta").path("content");
                    if (!delta.isMissingNode() && !delta.isNull()) {
                        String piece = delta.asText();
                        if (!piece.isEmpty()) {
                            full.append(piece);
                            onDelta.accept(piece);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("外部LLM流式调用失败: {}", e.getMessage());
        }
    }

    /** 最近若干条历史（含刚保存的当前提问），转为 messages */
    private List<Map<String, Object>> recentMessages(Long userId) {
        List<ChatMessage> list = chatMessageMapper.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .orderByDesc(ChatMessage::getCreateTime)
                .orderByDesc(ChatMessage::getId)
                .last("limit " + HISTORY_LIMIT));
        Collections.reverse(list);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChatMessage m : list) {
            String role = "assistant".equals(m.getRole()) ? "assistant" : "user";
            out.add(Map.of("role", role, "content", m.getContent() == null ? "" : m.getContent()));
        }
        return out;
    }

    private void emitChunks(String text, Consumer<String> onDelta) {
        for (int i = 0; i < text.length(); i += 4) {
            onDelta.accept(text.substring(i, Math.min(text.length(), i + 4)));
            try {
                Thread.sleep(12);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** 把检索到的商城资料拼成一段 system 上下文（RAG） */
    private String buildContext(List<KnowledgeDoc> docs) {
        if (docs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(
                "以下是商城内部资料（宠物档案 / 商品 / 规则）。若与用户问题相关，请优先依据这些资料回答，"
                        + "并在回答结尾简要注明来源（如《宠物档案》或「商城规则」）；资料无关则忽略。\n");
        for (KnowledgeDoc d : docs) {
            String label = switch (d.getType()) {
                case "breed" -> "宠物档案";
                case "product" -> "商品";
                default -> "规则";
            };
            sb.append("- [").append(label).append("] ").append(d.getTitle()).append("：")
                    .append(d.getContent().replace("\n", " ")).append("\n");
        }
        return sb.toString();
    }

    private String fallback(String q) {
        return "关于「" + q + "」，我暂时没能连上智能模型，先从养宠角度给你几点通用建议：\n" +
                "- **科学喂养**：定时定量，选择适合品种与年龄的粮，换粮用 7 天过渡；\n" +
                "- **健康管理**：定期驱虫与疫苗，关注体重和精神状态；\n" +
                "- **日常护理**：清洁耳道、牙齿与毛发，保持猫砂/窝垫干净；\n" +
                "- **异常就医**：出现持续呕吐、腹泻、拒食、精神差等情况及时就诊。\n\n" +
                "你可以再具体描述一下情况（品种、年龄、症状），我再帮你分析～";
    }

    private void save(Long userId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setUserId(userId);
        m.setRole(role);
        m.setContent(content);
        chatMessageMapper.insert(m);
    }
}
