package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 医生排班表 t_schedule（号源） */
@Data
@TableName("t_schedule")
public class Schedule implements Serializable {

    @TableId
    private Long id;
    private Long doctorId;
    private Long hospitalId;
    private Long departmentId;
    private LocalDate scheduleDate;
    /** 时间段，如 08:00-08:30 */
    private String timeSlot;
    /** 总号源 */
    private Integer totalCount;
    /** 剩余号源 */
    private Integer remainCount;
    /** 状态: 1可预约 0已约满 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
