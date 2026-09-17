package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 医院表 t_hospital */
@Data
@TableName("t_hospital")
public class Hospital implements Serializable {

    @TableId
    private Long id;
    private String name;
    /** 医院等级 */
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
    /** 状态: 1正常 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
