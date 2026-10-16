package com.mall.module.aftersale.dto;

import lombok.Data;

/**
 * 后台审核
 */
@Data
public class AfterSaleAuditDTO {

    /** true 通过（退款） / false 拒绝 */
    private Boolean pass;

    private String remark;
}
