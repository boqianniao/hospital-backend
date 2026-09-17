package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 医生表 t_doctor */
@Data
@TableName("t_doctor")
public class Doctor implements Serializable {

    @TableId
    private Long id;
    private String name;
    /** 性别: 1男 2女 */
    private Integer gender;
    private String title;
    private Long departmentId;
    private Long hospitalId;
    private String avatar;
    private String phone;
    private String intro;
    private String expertise;
    private Integer consultCount;
    private BigDecimal rating;
    private Integer followCount;
    /** 在线状态: 1在线 2离线 */
    private Integer onlineStatus;
    /** 咨询价格 */
    private BigDecimal price;
    /** 挂号价格 */
    private BigDecimal registrationPrice;
    /** 状态: 1正常 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
