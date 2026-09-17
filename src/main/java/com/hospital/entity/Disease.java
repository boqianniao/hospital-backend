package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 疾病表 t_disease */
@Data
@TableName("t_disease")
public class Disease implements Serializable {

    @TableId
    private Long id;
    /** 所属科室ID（二级科室） */
    private Long departmentId;
    private String name;
    private String description;
    private String alias;
    /** 发病部位 */
    private String location;
    /** 治疗方法 */
    private String treatment;
    /** 常见症状 */
    private String symptoms;
    /** 治疗周期 */
    private String treatmentPeriod;
    /** 治愈率 */
    private String cureRate;
    /** 临床检查 */
    private String examinations;
    private Integer followCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
