package com.mall.module.assistant.knowledge;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一条知识（品种 / 商品 / 规则）
 */
@Data
public class KnowledgeDoc {

    /** breed / product / rule */
    private String type;

    private String title;

    private String content;

    /** 关联 id（品种/商品） */
    private Long refId;

    /** 关键词（用于命中判断） */
    private List<String> keywords = new ArrayList<>();

    /** 用于 2-gram 匹配的短文本（标题 + 关键词，不含长正文以降噪） */
    private String matchText = "";

    private double score;
}
