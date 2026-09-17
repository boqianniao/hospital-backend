package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 咨询订单（含医生名称） */
@Data
public class ConsultVO implements Serializable {

    private Long id;
    private String orderNo;
    private Long doctorId;
    private String doctorName;
    private String patientName;
    private String patientPhone;
    private String diseaseDesc;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
    private Integer duration;
    private BigDecimal amount;
    /** 状态: 1待支付 2已支付 3咨询中 4已完成 5已取消 */
    private Integer status;
    private String statusText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
