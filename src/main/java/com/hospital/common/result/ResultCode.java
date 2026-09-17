package com.hospital.common.result;

import lombok.Getter;

/**
 * 统一业务状态码。
 * 说明：前端 auth-interceptor.js 约定 code==30001 表示 token 非法/过期，会跳转登录页，需保持一致。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数 / 请求
    PARAM_ERROR(40000, "参数错误"),
    NOT_FOUND(40004, "资源不存在"),
    METHOD_NOT_ALLOWED(40005, "请求方法不支持"),

    // 认证 / 授权
    UNAUTHORIZED(30000, "未登录或登录已失效"),
    TOKEN_INVALID(30001, "非法token或已过期"),
    LOGIN_FAIL(30002, "手机号或密码错误"),
    ACCOUNT_DISABLED(30003, "账号已被禁用"),
    VERIFY_CODE_ERROR(30004, "验证码错误或已过期"),
    OLD_PASSWORD_ERROR(30005, "原密码错误"),
    FORBIDDEN(30006, "无权限操作该资源"),

    // 业务
    USER_EXISTS(50001, "该手机号已注册"),
    USER_NOT_FOUND(50002, "用户不存在"),
    SCHEDULE_NOT_FOUND(50101, "排班不存在"),
    NO_STOCK(50102, "号源不足，请选择其他时段"),
    ORDER_NOT_FOUND(50103, "订单不存在"),
    ORDER_STATUS_ERROR(50104, "订单状态不允许该操作"),
    PAY_ERROR(50105, "支付失败"),
    RESOURCE_NOT_FOUND(50106, "数据不存在"),

    // 外部依赖
    SEARCH_UNAVAILABLE(60001, "搜索服务暂不可用");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
