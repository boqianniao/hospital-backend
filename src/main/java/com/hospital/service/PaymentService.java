package com.hospital.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hospital.common.constant.OrderStatus;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.config.props.HospitalProperties;
import com.hospital.entity.Appointment;
import com.hospital.entity.Consult;
import com.hospital.entity.PaymentFlow;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.ConsultMapper;
import com.hospital.mapper.PaymentFlowMapper;
import com.hospital.third.pay.AlipayService;
import com.hospital.vo.PayInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/** 下单、验签通知、主动查单和全额退款。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentFlowMapper paymentFlowMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final NotificationService notificationService;
    private final HospitalProperties props;
    private final AlipayService alipayService;

    @Transactional(rollbackFor = Exception.class)
    public PayInfoVO createPay(Long userId, String orderNo, int businessType, Integer payMethod) {
        OrderView order = loadOrder(orderNo, businessType);
        requireOwner(order, userId);
        if (order.paid || order.cancelled) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        int method = payMethod == null ? 1 : payMethod;
        if (method != 1) throw new BusinessException(ResultCode.PARAM_ERROR, "当前仅支持支付宝支付");
        if (order.amount == null || order.amount.signum() <= 0 || order.amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(ResultCode.PAY_ERROR, "订单金额无效");
        }
        if (!alipayService.isReady() && !alipayService.isMockEnabled()) {
            throw new BusinessException(ResultCode.PAY_ERROR, "支付宝支付未启用");
        }
        PaymentFlow flow = findFlow(orderNo, businessType);
        if (flow == null) {
            flow = new PaymentFlow();
            flow.setBusinessOrderNo(orderNo);
            flow.setBusinessType(businessType);
            flow.setPayMethod(method);
            flow.setActualAmount(order.amount);
            flow.setPayStatus(0);
            paymentFlowMapper.insert(flow);
        } else if (!Objects.equals(flow.getPayStatus(), 0) || !Objects.equals(flow.getPayMethod(), 1)
                || flow.getActualAmount().compareTo(order.amount) != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "支付流水状态或金额不匹配");
        }
        PayInfoVO vo = new PayInfoVO();
        vo.setOrderNo(orderNo);
        vo.setBusinessType(businessType);
        vo.setPayMethod(method);
        vo.setAmount(order.amount);
        if (alipayService.isReady()) {
            String subject = (businessType == OrderStatus.BIZ_APPOINTMENT ? "挂号订单-" : "咨询订单-") + orderNo;
            String form = alipayService.pagePay(subject, orderNo, order.amount.toPlainString(), props.getAlipay().getReturnUrl());
            if (!StringUtils.hasText(form)) throw new BusinessException(ResultCode.PAY_ERROR);
            vo.setMock(false);
            vo.setPayForm(form);
        } else {
            vo.setMock(true);
            vo.setPayForm("MOCK_PAY_FORM");
            vo.setMockNotifyUrl("/api/pay/mock/success?orderNo=" + orderNo + "&businessType=" + businessType);
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlipayNotify(Map<String, String> params) {
        if (!alipayService.verifyNotify(params)
                || !Objects.equals(props.getAlipay().getSellerId(), params.get("seller_id"))) return false;
        String status = params.get("trade_status");
        if (!StringUtils.hasText(params.get("out_trade_no")) || !StringUtils.hasText(params.get("trade_no"))) return false;
        if (!isPaidStatus(status)) return "WAIT_BUYER_PAY".equals(status) || "TRADE_CLOSED".equals(status);
        return applyVerifiedPayment(params.get("out_trade_no"), params.get("trade_no"), params.get("total_amount"), "notify:" + status);
    }

    /** 浏览器返回只能触发验签后的服务端查单，不能直接标记支付成功。 */
    @Transactional(rollbackFor = Exception.class)
    public AlipayReturnResult handleAlipayReturn(Map<String, String> params) {
        if (!alipayService.verifyNotify(params) || !StringUtils.hasText(params.get("out_trade_no"))) {
            return AlipayReturnResult.unverified();
        }
        String orderNo = params.get("out_trade_no");
        PaymentFlow flow = paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo).orderByDesc(PaymentFlow::getId).last("limit 1"));
        if (flow == null || flow.getBusinessType() == null) return AlipayReturnResult.unverified();
        boolean handled = syncAlipay(orderNo);
        OrderView order = loadOrder(orderNo, flow.getBusinessType());
        boolean paid = handled && !order.cancelled;
        return new AlipayReturnResult(paid, orderNo, flow.getBusinessType(), order.id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean queryPay(Long userId, String orderNo, int businessType) {
        OrderView order = loadOrder(orderNo, businessType);
        requireOwner(order, userId);
        if (alipayService.isMockEnabled()) return order.paid;
        // 已支付订单也查远端，避免将历史模拟流水当成沙箱支付凭据。
        boolean handled = syncAlipay(orderNo);
        return handled && !order.cancelled;
    }

    private boolean syncAlipay(String orderNo) {
        var trade = alipayService.query(orderNo);
        if (trade == null || !isPaidStatus(trade.getTradeStatus())) return false;
        return applyVerifiedPayment(orderNo, trade.getTradeNo(), trade.getTotalAmount(), "query:" + trade.getTradeStatus());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean mockSuccess(Long userId, String orderNo, int businessType) {
        if (!alipayService.isMockEnabled()) throw new BusinessException(ResultCode.FORBIDDEN, "模拟支付未启用");
        OrderView order = loadOrder(orderNo, businessType);
        requireOwner(order, userId);
        PaymentFlow flow = findFlow(orderNo, businessType);
        if (flow == null || order.cancelled) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        return applyPayment(orderNo, order, flow, "MOCK_" + orderNo, "mock");
    }

    private boolean applyVerifiedPayment(String orderNo, String tradeNo, String totalAmount, String source) {
        PaymentFlow found = paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo).orderByDesc(PaymentFlow::getId).last("limit 1"));
        if (found == null || found.getBusinessType() == null || !StringUtils.hasText(tradeNo)) return false;
        OrderView order = loadOrder(orderNo, found.getBusinessType());
        PaymentFlow flow = findFlow(orderNo, found.getBusinessType());
        try {
            BigDecimal amount = new BigDecimal(totalAmount);
            if (amount.signum() <= 0 || order.amount.compareTo(amount) != 0
                    || flow.getActualAmount().compareTo(amount) != 0 || !Objects.equals(flow.getPayMethod(), 1)) return false;
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
        return applyPayment(orderNo, order, flow, tradeNo, source);
    }

    private boolean applyPayment(String orderNo, OrderView order, PaymentFlow flow, String tradeNo, String source) {
        if (Objects.equals(flow.getPayStatus(), 1) || Objects.equals(flow.getPayStatus(), 2)) {
            return tradeNo.equals(flow.getThirdPartyTradeNo());
        }
        if (!Objects.equals(flow.getPayStatus(), 0)) return false;
        LocalDateTime now = LocalDateTime.now();
        if (order.cancelled) {
            // 取消后才收到成功通知时，以同一个退款请求号重试全额退款。
            if (!alipayService.refund(orderNo, order.amount.toPlainString())) return false;
            markFlow(flow, 2, tradeNo, source, now);
            return true;
        }
        if (!order.paid) {
            if (flow.getBusinessType() == OrderStatus.BIZ_APPOINTMENT) {
                Appointment update = new Appointment();
                update.setId(order.id);
                update.setStatus(OrderStatus.APPT_PAID);
                update.setPayTime(now);
                appointmentMapper.updateById(update);
            } else {
                Consult update = new Consult();
                update.setId(order.id);
                update.setStatus(OrderStatus.CONSULT_PAID);
                update.setPayTime(now);
                consultMapper.updateById(update);
            }
            notificationService.push(order.userId, "支付成功", String.format("您的订单 %s 已支付成功，金额 ¥%s。", orderNo, order.amount));
        }
        markFlow(flow, 1, tradeNo, source, now);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refund(String orderNo, int businessType, BigDecimal amount, Long userId) {
        OrderView order = loadOrder(orderNo, businessType);
        if (userId != null) requireOwner(order, userId);
        PaymentFlow paid = findFlow(orderNo, businessType);
        if (paid == null || amount == null || amount.compareTo(paid.getActualAmount()) != 0 || amount.compareTo(order.amount) != 0) {
            throw new BusinessException(ResultCode.PAY_ERROR, "退款流水或金额不匹配");
        }
        if (Objects.equals(paid.getPayStatus(), 2)) return;
        if (!Objects.equals(paid.getPayStatus(), 1)) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        boolean mock = paid.getThirdPartyTradeNo() != null && paid.getThirdPartyTradeNo().startsWith("MOCK_");
        if (mock ? !alipayService.isMockEnabled() : !alipayService.refund(orderNo, amount.toPlainString())) {
            throw new BusinessException(ResultCode.PAY_ERROR, "退款未成功，请稍后重试");
        }
        PaymentFlow update = new PaymentFlow();
        update.setId(paid.getId());
        update.setPayStatus(2);
        paymentFlowMapper.updateById(update);
        if (userId != null) notificationService.push(userId, "退款通知", String.format("您的订单 %s 已退款 ¥%s。", orderNo, amount));
    }

    private PaymentFlow findFlow(String orderNo, int businessType) {
        return paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo).eq(PaymentFlow::getBusinessType, businessType)
                .orderByDesc(PaymentFlow::getId).last("limit 1 for update"));
    }

    private void markFlow(PaymentFlow flow, int status, String tradeNo, String source, LocalDateTime now) {
        PaymentFlow update = new PaymentFlow();
        update.setId(flow.getId());
        update.setPayStatus(status);
        update.setThirdPartyTradeNo(tradeNo);
        update.setPaySuccessTime(now);
        update.setOriginalCallback(source);
        paymentFlowMapper.updateById(update);
    }

    private static boolean isPaidStatus(String status) {
        return "TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status);
    }

    private static void requireOwner(OrderView order, Long userId) {
        if (!Objects.equals(order.userId, userId)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    private OrderView loadOrder(String orderNo, int businessType) {
        OrderView v = new OrderView();
        if (businessType == OrderStatus.BIZ_APPOINTMENT) {
            Appointment a = appointmentMapper.selectOne(Wrappers.<Appointment>lambdaQuery()
                    .eq(Appointment::getOrderNo, orderNo).last("for update"));
            if (a == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            v.id = a.getId(); v.userId = a.getUserId(); v.amount = a.getAmount();
            v.paid = Objects.equals(a.getStatus(), OrderStatus.APPT_PAID) || Objects.equals(a.getStatus(), OrderStatus.APPT_DONE);
            v.cancelled = Objects.equals(a.getStatus(), OrderStatus.APPT_CANCELLED);
        } else if (businessType == OrderStatus.BIZ_CONSULT) {
            Consult c = consultMapper.selectOne(Wrappers.<Consult>lambdaQuery()
                    .eq(Consult::getOrderNo, orderNo).last("for update"));
            if (c == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            v.id = c.getId(); v.userId = c.getUserId(); v.amount = c.getAmount();
            v.paid = Objects.equals(c.getStatus(), OrderStatus.CONSULT_PAID) || Objects.equals(c.getStatus(), OrderStatus.CONSULT_ING)
                    || Objects.equals(c.getStatus(), OrderStatus.CONSULT_DONE);
            v.cancelled = Objects.equals(c.getStatus(), OrderStatus.CONSULT_CANCELLED);
        } else throw new BusinessException(ResultCode.PARAM_ERROR, "未知业务类型");
        return v;
    }

    private static class OrderView {
        Long id;
        Long userId;
        BigDecimal amount;
        boolean paid;
        boolean cancelled;
    }

    public record AlipayReturnResult(boolean paid, String orderNo, Integer businessType, Long orderId) {
        public static AlipayReturnResult unverified() {
            return new AlipayReturnResult(false, null, null, null);
        }
    }
}
