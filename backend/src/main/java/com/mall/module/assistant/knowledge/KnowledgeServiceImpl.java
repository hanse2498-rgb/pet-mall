package com.mall.module.assistant.knowledge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mall.module.pet.entity.PetBreed;
import com.mall.module.pet.mapper.PetBreedMapper;
import com.mall.module.product.dto.ProductQuery;
import com.mall.module.product.service.ProductService;
import com.mall.module.product.vo.ProductCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 关键词检索式轻量 RAG。
 * <p>知识来源：宠物档案（品种）、商品、商城规则。命中打分 = 关键词包含 + 2-gram 重叠。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    /** 命中阈值：至少一个 2 字关键词 / 一次 2-gram 命中 */
    private static final double THRESHOLD = 2.0;
    /** 最多返回条数 */
    private static final int LIMIT = 4;

    /** 品种常见简称 -> 便于命中（如「英短」-> 英国短毛猫） */
    private static final Map<String, String[]> BREED_ALIAS = buildAlias();

    private final PetBreedMapper petBreedMapper;
    private final ProductService productService;

    @Override
    public List<KnowledgeDoc> retrieve(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        String q = question.trim();
        List<KnowledgeDoc> scored = new ArrayList<>();
        for (KnowledgeDoc doc : loadDocs()) {
            double s = score(q, doc);
            if (s >= THRESHOLD) {
                doc.setScore(s);
                scored.add(doc);
            }
        }
        scored.sort(Comparator.comparingDouble(KnowledgeDoc::getScore).reversed());
        return scored.size() > LIMIT ? scored.subList(0, LIMIT) : scored;
    }

    // ---------- 语料加载 ----------

    private List<KnowledgeDoc> loadDocs() {
        List<KnowledgeDoc> docs = new ArrayList<>();
        docs.addAll(breedDocs());
        docs.addAll(productDocs());
        docs.addAll(ruleDocs());
        return docs;
    }

    private List<KnowledgeDoc> breedDocs() {
        List<PetBreed> breeds = petBreedMapper.selectList(
                Wrappers.<PetBreed>lambdaQuery().eq(PetBreed::getStatus, 1));
        List<KnowledgeDoc> docs = new ArrayList<>();
        for (PetBreed b : breeds) {
            KnowledgeDoc d = new KnowledgeDoc();
            d.setType("breed");
            d.setRefId(b.getId());
            d.setTitle(b.getName());
            String content = "**" + b.getName() + "**（" + safe(b.getEnName()) + "）\n"
                    + "- 体型：" + safe(b.getSize()) + "；毛长：" + safe(b.getHair())
                    + "；性格：" + safe(b.getCharacterTrait()) + "\n"
                    + "- 原产地：" + safe(b.getOrigin()) + "；寿命：" + safe(b.getLifeSpan())
                    + "；饲养难度：" + stars(b.getDifficulty()) + "\n"
                    + "- 简介：" + safe(b.getIntro()) + "\n"
                    + "- 详细介绍：" + safe(b.getDescription());
            d.setContent(content);
            d.getKeywords().add(b.getName());
            if (b.getEnName() != null) {
                d.getKeywords().add(b.getEnName().toLowerCase(Locale.ROOT));
            }
            String[] alias = BREED_ALIAS.get(b.getName());
            if (alias != null) {
                d.getKeywords().addAll(List.of(alias));
            }
            d.setMatchText(b.getName() + " " + safe(b.getEnName()) + " " + safe(b.getCharacterTrait()));
            docs.add(d);
        }
        return docs;
    }

    private List<KnowledgeDoc> productDocs() {
        ProductQuery query = new ProductQuery();
        query.setPageNum(1);
        query.setPageSize(500);
        List<ProductCardVO> products = productService.pageCards(query).getList();
        List<KnowledgeDoc> docs = new ArrayList<>();
        for (ProductCardVO p : products) {
            KnowledgeDoc d = new KnowledgeDoc();
            d.setType("product");
            d.setRefId(p.getId());
            d.setTitle(p.getName());
            String price = p.getMinPrice() == null ? "—" : "¥" + p.getMinPrice();
            d.setContent(price + " · " + safe(p.getBrandName()) + " · " + safe(p.getCategoryName())
                    + (p.getIsNearExpiry() != null && p.getIsNearExpiry() == 1 ? " · 临期7折" : ""));
            d.getKeywords().add(p.getName());
            if (p.getBrandName() != null) {
                d.getKeywords().add(p.getBrandName());
            }
            if (p.getCategoryName() != null) {
                d.getKeywords().add(p.getCategoryName());
            }
            d.setMatchText(p.getName() + " " + safe(p.getBrandName()) + " " + safe(p.getCategoryName()));
            docs.add(d);
        }
        return docs;
    }

    private List<KnowledgeDoc> ruleDocs() {
        List<KnowledgeDoc> docs = new ArrayList<>();
        docs.add(rule("运费规则", "满 200 元包邮，未满收取 10 元运费。",
                List.of("运费", "包邮", "邮费", "配送费")));
        docs.add(rule("售后规则", "用品支持签收后 7 天无理由退货；食品仅质量问题可申请售后；临期商品不支持无理由退货。",
                List.of("退货", "退款", "售后", "七天", "7天", "无理由")));
        docs.add(rule("临期规则", "剩余保质期 ≤ 1/3 的食品按 7 折销售（临期专区），临期商品不支持订阅。",
                List.of("临期", "保质期", "打折", "7折", "七折", "特价")));
        docs.add(rule("发货说明", "订单支付后尽快发货，物流公司与运单号可在订单详情查看。",
                List.of("发货", "物流", "快递", "多久到", "什么时候到")));
        return docs;
    }

    private KnowledgeDoc rule(String title, String content, List<String> keywords) {
        KnowledgeDoc d = new KnowledgeDoc();
        d.setType("rule");
        d.setTitle(title);
        d.setContent(content);
        d.getKeywords().addAll(keywords);
        d.setMatchText(title + " " + String.join(" ", keywords));
        return d;
    }

    // ---------- 打分 ----------

    private double score(String q, KnowledgeDoc doc) {
        double s = 0;
        // 1) 显式关键词包含
        for (String kw : doc.getKeywords()) {
            if (kw != null && !kw.isBlank() && q.contains(kw)) {
                s += Math.max(2, kw.length());
            }
        }
        // 2) 2-gram 重叠（针对标题/关键词短文本，降噪）
        String text = doc.getMatchText();
        for (int i = 0; i + 2 <= q.length(); i++) {
            String g = q.substring(i, i + 2);
            if (isCjk(g) && text.contains(g)) {
                s += 2;
            }
        }
        return s;
    }

    private boolean isCjk(String s) {
        return s.length() == 2 && isCjk(s.charAt(0)) && isCjk(s.charAt(1));
    }

    private boolean isCjk(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String stars(Integer difficulty) {
        int d = difficulty == null ? 0 : Math.max(0, Math.min(3, difficulty));
        return "★".repeat(d) + "☆".repeat(3 - d);
    }

    private static Map<String, String[]> buildAlias() {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("英国短毛猫", new String[]{"英短"});
        m.put("美国短毛猫", new String[]{"美短"});
        m.put("布偶猫", new String[]{"布偶"});
        m.put("暹罗猫", new String[]{"暹罗"});
        m.put("缅因猫", new String[]{"缅因"});
        m.put("波斯猫", new String[]{"波斯"});
        m.put("异国短毛猫（加菲）", new String[]{"加菲"});
        m.put("中华狸花猫", new String[]{"狸花"});
        m.put("斯芬克斯无毛猫", new String[]{"无毛猫", "斯芬克斯"});
        m.put("金毛寻回犬", new String[]{"金毛"});
        m.put("泰迪（贵宾犬）", new String[]{"泰迪", "贵宾"});
        m.put("哈士奇", new String[]{"二哈"});
        m.put("边境牧羊犬", new String[]{"边牧"});
        return m;
    }
}
