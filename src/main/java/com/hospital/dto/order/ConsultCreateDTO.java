package com.hospital.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电话咨询下单。
 */
@Data
public class ConsultCreateDTO {

    @NotNull(message = "请选择医生")
    private Long doctorId;

    /** 就诊成员ID（优先） */
    private Long familyMemberId;
    private String patientName;
    private String patientPhone;

    @NotBlank(message = "请填写病情描述")
    private String diseaseDesc;

    /** 预约咨询时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;

    /** 咨询时长(分钟)，默认30 */
    private Integer duration = 30;
}
