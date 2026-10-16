package com.mall.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),

    // 业务级（可按模块扩充）
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    GOODS_NOT_EXIST(2001, "商品不存在"),
    STOCK_NOT_ENOUGH(2002, "库存不足"),
    ORDER_STATUS_ERROR(3001, "订单状态不允许该操作");

    private final Integer code;
    private final String message;
}