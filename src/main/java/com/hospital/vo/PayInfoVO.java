package com.hospital.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 发起支付返回信息。
 * 桩阶段 payForm/payUrl 为占位，mock=true 提示可用 mock 回调完成支付；
 * 接入真实支付宝后 payForm 为可提交的表单/跳转链接。
 */
@Data
public class PayInfoVO {

    private String orderNo;
    private Integer businessType;
    private Integer payMethod;
    private BigDecimal amount;
    /** 支付宝返回的表单HTML或支付跳转链接（桩阶段为占位） */
    private String payForm;
    /** 是否桩支付 */
    private boolean mock;
    /** 桩阶段：模拟支付成功的回调地址，便于联调 */
    private String mockNotifyUrl;
}
