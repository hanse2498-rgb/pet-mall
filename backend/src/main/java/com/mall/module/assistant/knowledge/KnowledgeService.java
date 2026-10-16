package com.mall.module.assistant.knowledge;

import java.util.List;

/**
 * 轻量 RAG：关键词检索（宠物档案 + 商品 + 规则）
 */
public interface KnowledgeService {

    /** 依据问题检索相关知识点（已按相关度排序，仅返回达到阈值的） */
    List<KnowledgeDoc> retrieve(String question);
}
