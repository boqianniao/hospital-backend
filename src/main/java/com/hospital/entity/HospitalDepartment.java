package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 医院-科室关联表 t_hospital_department */
@Data
@TableName("t_hospital_department")
public class HospitalDepartment implements Serializable {

    @TableId
    private Long id;
    private Long hospitalId;
    private Long departmentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
