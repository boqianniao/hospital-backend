package com.hospital.controller;

import com.hospital.common.constant.OrderStatus;
import com.hospital.common.context.UserContext;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.Result;
import com.hospital.common.result.ResultCode;
import com.hospital.dto.order.PayCreateDTO;
import com.hospital.service.PaymentService;
import com.hospital.vo.PayInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "支付", description = "支付宝沙箱下单、通知和查单")
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {
    private final PaymentService paymentService;

    @Operation(summary = "发起支付宝支付")
    @PostMapping("/create")
    public Result<PayInfoVO> create(@Valid @RequestBody PayCreateDTO dto) {
        return Result.success(paymentService.createPay(UserContext.requireUserId(), dto.getOrderNo(), dto.getBusinessType(), dto.getPayMethod()));
    }

    @Operation(summary = "支付宝异步回调")
    @PostMapping(value = "/alipay/notify", produces = "text/plain;charset=UTF-8")
    public String alipayNotify(HttpServletRequest request) {
        try {
            return paymentService.handleAlipayNotify(singleParams(request)) ? "success" : "failure";
        } catch (RuntimeException e) {
            log.warn("支付宝通知处理失败 type={}", e.getClass().getSimpleName());
            return "failure";
        }
    }

    @Operation(summary = "支付宝浏览器同步返回")
    @GetMapping(value = "/alipay/return", produces = "text/html;charset=UTF-8")
    public String alipayReturn(HttpServletRequest request) {
        boolean paid = false;
        try {
            paid = paymentService.handleAlipayReturn(singleParams(request));
        } catch (RuntimeException e) {
            log.warn("支付宝返回查单失败 type={}", e.getClass().getSimpleName());
        }
        String message = paid ? "支付结果已确认，请返回订单页查看最新状态。" : "暂未确认支付结果，请返回订单页查询，勿重复付款。";
        return "<!doctype html><html lang=\"zh-CN\"><meta charset=\"utf-8\"><title>支付结果</title><body><p>" + message + "</p></body></html>";
    }

    @Operation(summary = "查询并同步支付宝支付结果（需订单本人登录）")
    @PostMapping("/query")
    public Result<Boolean> query(@Valid @RequestBody PayCreateDTO dto) {
        return Result.success(paymentService.queryPay(UserContext.requireUserId(), dto.getOrderNo(), dto.getBusinessType()));
    }

    @Operation(summary = "模拟支付成功（仅显式启用模拟支付时可用）")
    @PostMapping("/mock/success")
    public Result<Void> mockSuccess(@RequestParam String orderNo, @RequestParam(required = false) Integer businessType) {
        int biz = businessType != null ? businessType : bizTypeOf(orderNo);
        boolean ok = paymentService.mockSuccess(UserContext.requireUserId(), orderNo, biz);
        return ok ? Result.success("支付成功", null) : Result.fail("支付处理失败");
    }

    private static Map<String, String> singleParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length != 1) throw new BusinessException(ResultCode.PARAM_ERROR);
            params.put(key, values[0]);
        });
        return params;
    }

    private static int bizTypeOf(String orderNo) {
        if (orderNo != null && orderNo.startsWith("ZX")) return OrderStatus.BIZ_CONSULT;
        if (orderNo != null && orderNo.startsWith("GH")) return OrderStatus.BIZ_APPOINTMENT;
        throw new BusinessException(ResultCode.PARAM_ERROR, "未知业务订单号");
    }
}
