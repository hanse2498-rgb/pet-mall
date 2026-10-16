package com.mall.module.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 发布动态请求
 */
@Data
public class PostCreateRequest {

    private String content;

    private List<String> images;

    private Long petId;
}