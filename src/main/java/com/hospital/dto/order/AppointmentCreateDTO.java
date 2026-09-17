package com.hospital.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预约挂号下单。号源(scheduleId)决定医生/医院/日期/时段；
 * 就诊人可传 familyMemberId 自动带出，或直接传姓名/电话等字段。
 */
@Data
public class AppointmentCreateDTO {

    @NotNull(message = "请选择号源")
    private Long scheduleId;

    /** 就诊成员ID（优先） */
    private Long familyMemberId;

    private String patientName;
    private String patientPhone;
    private String patientIdCard;
    /** 性别: 1男 2女 */
    private Integer patientGender;
    private Integer patientAge;

    private String diseaseDesc;
}
