package com.hospital.third.sms;

/**
 * 短信服务抽象。当前为桩实现（打印日志）；后续可接入阿里云/腾讯云短信只需替换实现。
 */
public interface SmsService {

    /**
     * 发送验证码短信。
     * @param phone 手机号
     * @param code  验证码
     */
    void sendVerifyCode(String phone, String code);
}
