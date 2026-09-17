package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付流水表 t_payment_flow */
@Data
@TableName("t_payment_flow")
public class PaymentFlow implements Serializable {

    @TableId
    private Long id;
    /** 业务订单号（挂号或咨询订单号） */
    private String businessOrderNo;
    /** 业务类型: 1挂号 2咨询 */
    private Integer businessType;
    /** 支付方式: 1支付宝 2微信 */
    private Integer payMethod;
    private String thirdPartyTradeNo;
    private BigDecimal actualAmount;
    /** 支付状态: 0待支付 1已支付 2已退款 3已关闭 */
    private Integer payStatus;
    private LocalDateTime paySuccessTime;
    /** 原始回调报文 */
    private String originalCallback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
