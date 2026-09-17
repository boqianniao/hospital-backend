package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 挂号订单（含医生/医院名称） */
@Data
public class AppointmentVO implements Serializable {

    private Long id;
    private String orderNo;
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private String patientName;
    private String patientPhone;
    private String patientIdCard;
    private Integer patientGender;
    private Integer patientAge;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String diseaseDesc;
    private BigDecimal amount;
    /** 状态: 1待支付 2已支付 3已完成 4已取消 */
    private Integer status;
    private String statusText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
