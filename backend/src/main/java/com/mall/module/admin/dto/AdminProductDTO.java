package com.mall.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 后台商品维护 DTO
 */
public class AdminProductDTO {

    @Data
    public static class ProductForm {
        private Long id;
        private Long brandId;
        private Long categoryId;
        @NotBlank(message = "商品名不能为空")
        private String name;
        private String subtitle;
        private String coverImg;
        private List<String> images;
        private String detailHtml;
        private Integer saleType;
        private Integer petType;
        private Integer shelfStatus;
    }

    @Data
    public static class SkuForm {
        private Long id;
        private Long productId;
        private String specName;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
        private Integer status;
    }

    @Data
    public static class BatchForm {
        private Long skuId;
        private String batchNo;
        private LocalDate produceDate;
        private Integer expiryDays;
    }
}