package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/** 医生排班（号源） */
@Data
public class ScheduleVO {

    private Long id;
    private Long doctorId;
    private Long hospitalId;
    private Long departmentId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    private String timeSlot;
    private Integer totalCount;
    private Integer remainCount;
    /** 状态: 1可预约 0已约满 */
    private Integer status;
}
