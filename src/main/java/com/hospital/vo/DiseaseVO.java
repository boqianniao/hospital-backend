package com.hospital.vo;

import lombok.Data;

import java.io.Serializable;

/** 疾病信息（含科室名称） */
@Data
public class DiseaseVO implements Serializable {

    private Long id;
    private Long departmentId;
    private String departmentName;
    private String name;
    private String description;
    private String alias;
    private String location;
    private String treatment;
    private String symptoms;
    private String treatmentPeriod;
    private String cureRate;
    private String examinations;
    private Integer followCount;
}
