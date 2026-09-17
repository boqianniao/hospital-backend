package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 关注表 t_follow */
@Data
@TableName("t_follow")
public class Follow implements Serializable {

    @TableId
    private Long id;
    private Long userId;
    /** 关注类型: 1医院 2医生 3疾病 */
    private Integer followType;
    /** 关注对象ID */
    private Long followId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
