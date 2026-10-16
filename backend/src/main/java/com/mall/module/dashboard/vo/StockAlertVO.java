package com.mall.module.dashboard.vo;

import lombok.Data;

/**
 * 库存预警
 */
@Data
public class StockAlertVO {
    private String productName;
    private String spec;
    private Integer stock;
}
