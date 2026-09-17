package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 挂号订单表 t_appointment */
@Data
@TableName("t_appointment")
public class Appointment implements Serializable {

    @TableId
    private Long id;
    private String orderNo;
    private Long userId;
    private Long doctorId;
    private Long hospitalId;
    private String patientName;
    private String patientPhone;
    private String patientIdCard;
    /** 就诊人性别: 1男 2女 */
    private Integer patientGender;
    private Integer patientAge;
    private LocalDate appointmentDate;
    /** 预约时间段 */
    private String appointmentTime;
    private String diseaseDesc;
    private BigDecimal amount;
    /** 状态: 1待支付 2已支付 3已完成 4已取消 */
    private Integer status;
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
