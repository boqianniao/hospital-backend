package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 用户表 t_user */
@Data
@TableName("t_user")
public class User implements Serializable {

    @TableId
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String realName;
    /** 性别: 1男 2女 */
    private Integer gender;
    private LocalDate birthday;
    private String avatar;
    /** 状态: 1正常 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
