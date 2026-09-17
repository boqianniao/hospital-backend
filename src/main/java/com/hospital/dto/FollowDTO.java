package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 关注/取消关注 */
@Data
public class FollowDTO {

    /** 关注类型: 1医院 2医生 3疾病 */
    @NotNull(message = "关注类型不能为空")
    private Integer followType;

    @NotNull(message = "关注对象不能为空")
    private Long followId;
}
