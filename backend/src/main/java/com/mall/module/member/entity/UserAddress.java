package com.mall.module.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user_address")
public class UserAddress extends BaseEntity {

    private Long userId;

    /** 收货人 */
    private String receiverName;

    /** 收货电话 */
    private String phone;

    private String province;

    private String city;

    private String district;

    /** 详细地址 */
    private String detail;

    /** 是否默认：0否 1是 */
    private Integer isDefault;
}