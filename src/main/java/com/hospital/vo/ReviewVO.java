package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 评价（含用户/医生名称） */
@Data
public class ReviewVO {

    private Long id;
    private Integer orderType;
    private Long orderId;
    private Long userId;
    /** 用户名（脱敏） */
    private String userName;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String doctorAvatar;
    private String departmentName;
    private String hospitalName;
    private Integer rating;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
