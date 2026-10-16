package com.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台管理员
 */
@Data
@TableName("tb_admin_user")
public class AdminUser implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 账号 */
    private String username;

    /** 密码（BCrypt） */
    private String passwordHash;

    private String nickname;

    /** 0禁用 1启用 */
    private Integer status;

    private Long roleId;

    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}