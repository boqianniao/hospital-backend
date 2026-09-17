package com.hospital.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 医院详情：基础信息 + 关联科室 */
@Data
public class HospitalDetailVO implements Serializable {

    private Long id;
    private String name;
    private String level;
    private String address;
    private String phone;
    private String intro;
    private String image;
    private String province;
    private String city;
    private Integer departmentCount;
    private Integer doctorCount;
    private Integer followCount;
    /** 该医院开设的科室（一级，含二级 children） */
    private List<DepartmentVO> departments;
}
