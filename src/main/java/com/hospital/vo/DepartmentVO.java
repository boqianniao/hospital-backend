package com.hospital.vo;

import lombok.Data;

import java.util.List;

/** 科室（支持树形 children） */
@Data
public class DepartmentVO {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    /** 二级科室 */
    private List<DepartmentVO> children;
}
