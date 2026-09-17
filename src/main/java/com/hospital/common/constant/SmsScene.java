package com.hospital.common.constant;

/**
 * 验证码使用场景。
 */
public final class SmsScene {

    private SmsScene() {
    }

    /** 注册 */
    public static final String REGISTER = "register";
    /** 登录（验证码登录，可选） */
    public static final String LOGIN = "login";
    /** 重置/找回密码 */
    public static final String RESET = "reset";
}
