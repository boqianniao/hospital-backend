package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 提交反馈 */
@Data
public class FeedbackDTO {

    /** 反馈类型: 1系统问题 2服务问题 3医生问题 4其他问题 */
    @NotNull(message = "反馈类型不能为空")
    private Integer feedbackType;

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    /** 反馈图片(逗号分隔) */
    private String images;
}
