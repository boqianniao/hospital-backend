package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 科室表 t_department（自关联：parent_id=0 为一级科室） */
@Data
@TableName("t_department")
public class Department implements Serializable {

    @TableId
    private Long id;
    private String name;
    private String description;
    /** 父级科室ID，0=一级科室 */
    private Long parentId;
    private Integer sortOrder;
    /** 状态: 1正常 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
