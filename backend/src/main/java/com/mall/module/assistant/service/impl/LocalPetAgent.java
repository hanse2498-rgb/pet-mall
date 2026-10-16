package com.mall.module.assistant.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mall.module.assistant.entity.ChatMessage;
import com.mall.module.assistant.knowledge.KnowledgeDoc;
import com.mall.module.assistant.knowledge.KnowledgeService;
import com.mall.module.assistant.mapper.ChatMessageMapper;
import com.mall.module.assistant.service.AiService;
import com.mall.module.assistant.service.guard.PetTopicGuard;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

/**
 * 本地宠物知识库助手（默认实现）：按规则作答，再分块"打字机"推送。
 */
@Slf4j
public class LocalPetAgent implements AiService {

    /** 每次推送的字符数 */
    private static final int CHUNK = 4;
    /** 分块间隔（毫秒），营造打字机效果 */
    private static final long CHUNK_DELAY_MS = 12;

    private final ChatMessageMapper chatMessageMapper;
    private final KnowledgeService knowledgeService;

    public LocalPetAgent(ChatMessageMapper chatMessageMapper, KnowledgeService knowledgeService) {
        this.chatMessageMapper = chatMessageMapper;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public void replyStream(Long userId, String question, Consumer<String> onDelta) {
        if (question == null || question.isBlank()) {
            emit("有什么关于养宠的问题都可以问我～", onDelta);
            return;
        }
        String q = question.trim();
        save(userId, "user", q);

        // 检索知识库（宠物档案 / 商品 / 规则）
        List<KnowledgeDoc> docs = knowledgeService.retrieve(q);

        String answer;
        if (PetTopicGuard.isPetTopic(q)) {
            // ① 养宠常识规则优先
            String ruled = ruleAnswer(q);
            if (ruled != null) {
                answer = ruled;
            } else if (!docs.isEmpty()) {
                answer = knowledgeAnswer(docs);
            } else {
                answer = genericFallback(q);
            }
        } else if (!docs.isEmpty()) {
            // 非宠物关键词但命中知识库（如「英短」「运费」「临期」）→ 也算范围内
            answer = knowledgeAnswer(docs);
        } else {
            answer = PetTopicGuard.REFUSE;
        }
        save(userId, "assistant", answer);

        emit(answer, onDelta);
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

    /** 分块推送文本 */
    private void emit(String text, Consumer<String> onDelta) {
        for (int i = 0; i < text.length(); i += CHUNK) {
            onDelta.accept(text.substring(i, Math.min(text.length(), i + CHUNK)));
            try {
                Thread.sleep(CHUNK_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ---------- rules ----------

    private String ruleAnswer(String q) {
        if (q.contains("换粮") || (q.contains("换") && q.contains("粮"))) {
            return "**科学换粮建议用「7 天过渡法」**，让肠胃慢慢适应：\n" +
                    "- 第 1–2 天：新粮 1 份 + 旧粮 4 份\n" +
                    "- 第 3–4 天：新旧各半（2:2）\n" +
                    "- 第 5–6 天：新粮 4 份 + 旧粮 1 份\n" +
                    "- 第 7 天：完全换成新粮\n\n" +
                    "**注意事项**：\n" +
                    "- 换粮期间别同时换零食或罐头，一次只变一个变量；\n" +
                    "- 每天观察便便是否成型、食欲是否正常；\n" +
                    "- 若出现软便、呕吐或拒食，先放慢换粮速度，严重时停新粮并咨询兽医。";
        }
        if (q.contains("洗澡") || q.contains("沐浴")) {
            return "**洗澡频率**：\n" +
                    "- 猫咪：1–2 个月一次即可（猫会自我清洁，洗太勤会破坏皮脂层）；\n" +
                    "- 狗狗：夏天约 2 周一次、冬天约 1 个月一次，体味重或外出脏了可适当增加。\n\n" +
                    "**操作要点**：\n" +
                    "- 用**宠物专用**沐浴露，人的沐浴露偏酸会伤皮肤；\n" +
                    "- 水温 37–39℃，先冲湿再打泡，避开眼睛和耳朵；\n" +
                    "- 洗后**彻底吹干**（尤其腋下、腹部、耳道），未吹干易得皮肤病；\n" +
                    "- 洗澡前先梳毛去浮毛，洗后注意保暖别着凉。";
        }
        if (q.contains("驱虫")) {
            return "**驱虫分体内和体外，都要做**：\n" +
                    "- **体外**（跳蚤、蜱虫、螨虫）：滴剂/喷剂多见，一般每月一次；\n" +
                    "- **体内**（蛔虫、绦虫等）：口服药多见，幼宠与常外出的通常每 1–3 个月一次。\n\n" +
                    "**注意事项**：\n" +
                    "- 严格按**体重**确定剂量，别凭感觉给；\n" +
                    "- 体内外产品要错开时间，洗澡前后间隔 1–2 天再滴体外；\n" +
                    "- 刚驱虫后出现轻微嗜睡、食欲差多属正常，若呕吐/抽搐等异常及时就医；\n" +
                    "- 孕宠、幼龄宠请在兽医指导下选用对应产品。";
        }
        if (q.contains("疫苗")) {
            return "**幼宠首次免疫时间**：\n" +
                    "- 猫咪：约 8 周龄开始打猫三联，之后每 3–4 周一针，共 2–3 针；\n" +
                    "- 狗狗：约 6–8 周龄开始，接种犬类联苗（如六联/八联），同样分多针完成。\n\n" +
                    "**加强与注意**：\n" +
                    "- 首免完成后每年加强一次；狂犬疫苗按当地规定单独接种；\n" +
                    "- 疫苗前需健康、无腹泻、无发热；\n" +
                    "- 接种后观察 30 分钟，1–2 天内避免洗澡、剧烈运动和换环境；\n" +
                    "- 出现持续呕吐、面部肿胀、呼吸急促等过敏反应，立即就医。";
        }
        if (q.contains("猫砂")) {
            return "**猫砂日常维护**：\n" +
                    "- 每天抓铲 1–2 次，及时清掉结块和便便；\n" +
                    "- 约每周彻底更换并清洗消毒猫砂盆；\n" +
                    "- 铺 2–3cm 厚度，太薄猫难埋、太厚浪费。\n\n" +
                    "**换砂/换盆建议**：\n" +
                    "- 换品牌时新旧砂混合过渡，逐步增加新砂比例；\n" +
                    "- 猫咪挑剔时，多放一个盆或用它习惯的砂型；\n" +
                    "- 盆要放在安静、通风、远离食盆的位置。";
        }
        if (q.contains("遛狗")) {
            return "**遛狗频率与时长**：\n" +
                    "- 小型犬：每天 1–2 次，共 20–30 分钟；\n" +
                    "- 中大型犬/牧羊犬：每天 1–2 次，共 45–90 分钟，精力旺盛的更要多运动。\n\n" +
                    "**注意事项**：\n" +
                    "- 盛夏避开正午高温，谨防中暑；冬天注意保暖，雨雪后擦干爪子；\n" +
                    "- 出门**务必牵绳**，烦犬配对、追车追人风险高；\n" +
                    "- 幼犬未完成疫苗前尽量少去复杂环境；\n" +
                    "- 随身带水、捡便袋，回家检查脚垫与身上有无蜱虫。";
        }
        if (q.contains("挑食") || q.contains("不吃") || q.contains("厌食")) {
            return "**先判断是「真挑食」还是「生病」**：\n" +
                    "- 精神好、只是闻闻不吃 → 多为挑食或零食吃多了；\n" +
                    "- 伴随精神差、呕吐、腹泻、体重下降 → 优先考虑疾病，尽快就医。\n\n" +
                    "**改善挑食的做法**：\n" +
                    "- 定时定量喂，15–20 分钟不吃就收走，中间不补零食；\n" +
                    "- 减少人食和零食，保持主粮地位；\n" +
                    "- 适当加热或拌一点点湿粮/冻干提升适口性；\n" +
                    "- **猫咪连续拒食超过 24 小时**很危险，务必就医。";
        }
        if (q.contains("呕吐")) {
            return "**先区分轻重**：\n" +
                    "- 偶发吐毛球、吃太快吐未消化食物、精神仍好 → 一般观察即可；\n" +
                    "- 频繁呕吐、带血/黄绿胆汁、精神萎靡、伴随腹泻 → 尽早就医。\n\n" +
                    "**临时处理**：\n" +
                    "- 先禁食 6–12 小时，保证充足饮水；\n" +
                    "- 恢复后喂少量易消化的清淡食物，少量多餐；\n" +
                    "- 不要自行乱用止吐、抗生素等药物；\n" +
                    "- 若怀疑吞了异物（玩具、线、骨头），立即送医拍片。";
        }
        if (q.contains("腹泻") || q.contains("拉稀")) {
            return "**轻症可先居家观察**：\n" +
                    "- 暂时清淡饮食（如易消化处方粮），少量多餐；\n" +
                    "- 注意补水，可适当补充益生菌；\n" +
                    "- 检查是否换了粮、吃了不洁或人食。\n\n" +
                    "**出现以下情况请就医**：\n" +
                    "- 持续超过 24 小时、便中带血或发黑；\n" +
                    "- 伴随呕吐、发热、精神差、脱水；\n" +
                    "- 幼宠、老年宠腹泻进展快，更要及时就诊。";
        }
        if (q.contains("关节") || q.contains("老年") || q.contains("骨头")) {
            return "**中老年宠物的关节养护**：\n" +
                    "- 控制体重，肥胖是关节负担的头号因素；\n" +
                    "- 避免频繁上下楼梯、跳跃，家具旁放台阶；\n" +
                    "- 地面铺防滑垫，提供软硬适中的窝；\n" +
                    "- 可在兽医指导下补充氨糖软骨素、鱼油等；\n" +
                    "- 适度温和运动（散步、游泳）维持肌肉量；\n" +
                    "- 建议每半年到一年体检一次。";
        }
        return null;
    }

    // ---------- 检索式 RAG 作答 ----------

    private String knowledgeAnswer(List<KnowledgeDoc> docs) {
        if (docs.isEmpty()) {
            return genericFallback("这个问题");
        }
        KnowledgeDoc top = docs.get(0);
        if ("breed".equals(top.getType())) {
            return top.getContent() + "\n\n> 📖 资料来自《宠物档案》";
        }
        if ("product".equals(top.getType())) {
            StringBuilder sb = new StringBuilder("为你找到以下相关商品：\n");
            int n = 0;
            for (KnowledgeDoc d : docs) {
                if ("product".equals(d.getType()) && n < 5) {
                    sb.append("- **").append(d.getTitle()).append("**（").append(d.getContent()).append("）\n");
                    n++;
                }
            }
            if (n == 0) {
                return genericFallback("这个问题");
            }
            sb.append("\n想了解更多可以到「全部商品」按分类 / 品牌筛选～");
            return sb.toString();
        }
        // 规则类
        return top.getContent() + "\n\n> 📖 资料来自商城规则";
    }

    private String genericFallback(String q) {
        return "关于「" + q + "」，我从养宠角度给你几点建议：\n" +
                "- **科学喂养**：定时定量，选择适合品种与年龄的粮，换粮用 7 天过渡；\n" +
                "- **健康管理**：定期驱虫、疫苗，注意体重与日常状态；\n" +
                "- **日常护理**：清洁耳朵/牙齿/毛发，保持环境干净；\n" +
                "- **异常就医**：出现持续呕吐、腹泻、精神差、拒食等情况及时就诊。\n\n" +
                "你也可以问我某只**品种**（如「英短怎么样」）或某类**商品**（如「有什么罐头」），我给你更具体的～";
    }

    private void save(Long userId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setUserId(userId);
        m.setRole(role);
        m.setContent(content);
        chatMessageMapper.insert(m);
    }
}
