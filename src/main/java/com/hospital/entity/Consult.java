package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 电话咨询订单表 t_consult */
@Data
@TableName("t_consult")
public class Consult implements Serializable {

    @TableId
    private Long id;
    private String orderNo;
    private Long userId;
    private Long doctorId;
    private String patientName;
    private String patientPhone;
    private String diseaseDesc;
    /** 预约咨询时间 */
    private LocalDateTime appointmentTime;
    /** 咨询时长(分钟) */
    private Integer duration;
    private BigDecimal amount;
    /** 状态: 1待支付 2已支付 3咨询中 4已完成 5已取消 */
    private Integer status;
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
