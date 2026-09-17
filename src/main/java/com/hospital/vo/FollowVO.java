package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 我的关注项（含关注对象基础信息） */
@Data
public class FollowVO {

    private Long id;
    /** 关注类型: 1医院 2医生 3疾病 */
    private Integer followType;
    private Long followId;
    private String targetName;
    private String targetImage;
    private String targetIntro;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
