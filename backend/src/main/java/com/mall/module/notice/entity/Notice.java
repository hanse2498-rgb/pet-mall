package com.mall.module.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站内通知（订单 / 售后）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_notice")
public class Notice extends BaseEntity {

    private Long userId;

    /** 1订单 2售后 */
    private Integer type;

    private String title;

    private String content;

    /** 关联订单id（用于点击跳转） */
    private Long bizId;

    /** 0未读 1已读 */
    private Integer isRead;
}
