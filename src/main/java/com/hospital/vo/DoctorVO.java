package com.hospital.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 医生信息（含医院/科室名称） */
@Data
public class DoctorVO implements Serializable {

    private Long id;
    private String name;
    private Integer gender;
    private String title;
    private Long departmentId;
    private String departmentName;
    private Long hospitalId;
    private String hospitalName;
    private String avatar;
    private String intro;
    private String expertise;
    private Integer consultCount;
    private BigDecimal rating;
    private Integer followCount;
    private Integer onlineStatus;
    private BigDecimal price;
    private BigDecimal registrationPrice;
    private Integer status;
}
