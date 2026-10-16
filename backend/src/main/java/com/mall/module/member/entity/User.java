package com.mall.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
@TableName("tb_user")
public class User implements Serializable {

    /** 主键（雪花） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 手机号 */
    private String phone;

    /** 密码（预留，暂用验证码登录） */
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}