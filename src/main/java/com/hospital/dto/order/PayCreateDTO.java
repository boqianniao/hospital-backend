package com.hospital.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起支付。
 */
@Data
public class PayCreateDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /** 业务类型: 1挂号 2咨询 */
    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    /** 支付方式: 1支付宝 2微信，默认支付宝 */
    private Integer payMethod = 1;
}
