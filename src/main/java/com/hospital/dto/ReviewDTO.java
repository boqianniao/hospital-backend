package com.hospital.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 提交评价 */
@Data
public class ReviewDTO {

    /** 订单类型: 1挂号 2咨询 */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分为1-5")
    @Max(value = 5, message = "评分为1-5")
    private Integer rating;

    private String content;
}
