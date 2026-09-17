package com.hospital.controller;

import com.hospital.common.constant.OrderStatus;
import com.hospital.common.context.UserContext;
import com.hospital.common.result.Result;
import com.hospital.dto.order.PayCreateDTO;
import com.hospital.service.PaymentService;
import com.hospital.third.pay.AlipayService;
import com.hospital.vo.PayInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付：发起支付、支付宝异步回调、（桩）模拟支付成功。
 */
@Slf4j
@Tag(name = "支付", description = "发起支付/回调")
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PaymentService paymentService;
    private final AlipayService alipayService;

    @Operation(summary = "发起支付")
    @PostMapping("/create")
    public Result<PayInfoVO> create(@Valid @RequestBody PayCreateDTO dto) {
        return Result.success(paymentService.createPay(
                UserContext.requireUserId(), dto.getOrderNo(), dto.getBusinessType(), dto.getPayMethod()));
    }

    /**
     * 支付宝异步回调（白名单，无需登录）。业务类型由订单号前缀推断（GH挂号 / ZX咨询）。
     * 真实接入时需先验签再处理。返回 "success" 告知支付宝已收到。
     */
    @Operation(summary = "支付宝异步回调")
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().length > 0 ? e.getValue()[0] : ""));
        // 真实接入时先验签，验签不过直接拒绝，防止伪造回调
        if (alipayService.isReady() && !alipayService.verifyNotify(params)) {
            log.warn("支付宝回调验签失败 out_trade_no={}", params.get("out_trade_no"));
            return "failure";
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        if (outTradeNo == null) {
            return "failure";
        }
        if (tradeStatus == null || "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            int businessType = bizTypeOf(outTradeNo);
            boolean ok = paymentService.handlePaySuccess(outTradeNo, businessType, tradeNo, params.toString());
            return ok ? "success" : "failure";
        }
        return "success";
    }

    /**
     * 桩：模拟支付成功（仅本地/未接支付宝时联调用）。需登录。
     */
    @Operation(summary = "模拟支付成功（桩）")
    @PostMapping("/mock/success")
    public Result<Void> mockSuccess(@RequestParam String orderNo,
                                    @RequestParam(required = false) Integer businessType) {
        UserContext.requireUserId();
        int biz = businessType != null ? businessType : bizTypeOf(orderNo);
        boolean ok = paymentService.handlePaySuccess(orderNo, biz, "MOCK_" + System.currentTimeMillis(), "mock");
        return ok ? Result.success("支付成功", null) : Result.fail("支付处理失败");
    }

    private int bizTypeOf(String orderNo) {
        return orderNo != null && orderNo.startsWith("ZX")
                ? OrderStatus.BIZ_CONSULT : OrderStatus.BIZ_APPOINTMENT;
    }
}
