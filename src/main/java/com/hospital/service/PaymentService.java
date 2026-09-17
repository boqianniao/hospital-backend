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
import com.hospital.vo.PayInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付服务：发起支付（支付宝沙箱-桩）、回调/模拟支付成功处理（幂等）、退款（桩）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFlowMapper paymentFlowMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final NotificationService notificationService;
    private final HospitalProperties props;

    /** 发起支付：校验订单归属与状态，创建/复用待支付流水，返回支付信息 */
    @Transactional(rollbackFor = Exception.class)
    public PayInfoVO createPay(Long userId, String orderNo, int businessType, Integer payMethod) {
        OrderView order = loadOrder(orderNo, businessType);
        if (!order.userId.equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.paid) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单已支付，请勿重复支付");
        }
        if (order.cancelled) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单已取消，无法支付");
        }
        int method = payMethod == null ? 1 : payMethod;

        // 复用待支付流水或新建
        PaymentFlow flow = paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo)
                .eq(PaymentFlow::getPayStatus, 0)
                .last("limit 1"));
        if (flow == null) {
            flow = new PaymentFlow();
            flow.setBusinessOrderNo(orderNo);
            flow.setBusinessType(businessType);
            flow.setPayMethod(method);
            flow.setActualAmount(order.amount);
            flow.setPayStatus(0);
            paymentFlowMapper.insert(flow);
        }

        PayInfoVO vo = new PayInfoVO();
        vo.setOrderNo(orderNo);
        vo.setBusinessType(businessType);
        vo.setPayMethod(method);
        vo.setAmount(order.amount);
        if (props.getAlipay().isEnabled()) {
            // TODO: 调用支付宝 SDK 生成支付表单/链接
            vo.setMock(false);
            vo.setPayForm("<!-- 待接入支付宝沙箱：AlipayClient.pageExecute 返回的表单 -->");
        } else {
            vo.setMock(true);
            vo.setPayForm("MOCK_PAY_FORM");
            vo.setMockNotifyUrl("/api/pay/mock/success?orderNo=" + orderNo + "&businessType=" + businessType);
        }
        return vo;
    }

    /**
     * 支付成功处理（支付宝回调 / 模拟回调统一入口），幂等。
     * @return true 处理成功或已处理
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaySuccess(String orderNo, int businessType, String thirdTradeNo, String rawCallback) {
        OrderView order = loadOrder(orderNo, businessType);
        LocalDateTime now = LocalDateTime.now();

        // 幂等：已支付则仅补齐流水
        if (order.paid) {
            markFlowPaid(orderNo, businessType, order.amount, thirdTradeNo, rawCallback, now);
            return true;
        }
        if (order.cancelled) {
            log.warn("支付回调命中已取消订单 orderNo={}", orderNo);
            return false;
        }

        // 更新订单为已支付
        if (businessType == OrderStatus.BIZ_APPOINTMENT) {
            Appointment a = new Appointment();
            a.setId(order.id);
            a.setStatus(OrderStatus.APPT_PAID);
            a.setPayTime(now);
            appointmentMapper.updateById(a);
        } else {
            Consult c = new Consult();
            c.setId(order.id);
            c.setStatus(OrderStatus.CONSULT_PAID);
            c.setPayTime(now);
            consultMapper.updateById(c);
        }
        markFlowPaid(orderNo, businessType, order.amount, thirdTradeNo, rawCallback, now);

        notificationService.push(order.userId, "支付成功",
                String.format("您的订单 %s 已支付成功，金额 ¥%s。", orderNo, order.amount));
        return true;
    }

    /** 退款（桩）：标记流水已退款。真实支付宝退款后续接入 */
    @Transactional(rollbackFor = Exception.class)
    public void refund(String orderNo, int businessType, BigDecimal amount, Long userId) {
        PaymentFlow paid = paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo)
                .eq(PaymentFlow::getPayStatus, 1)
                .last("limit 1"));
        if (paid != null) {
            PaymentFlow upd = new PaymentFlow();
            upd.setId(paid.getId());
            upd.setPayStatus(2); // 已退款
            paymentFlowMapper.updateById(upd);
            if (props.getAlipay().isEnabled()) {
                // TODO: 调用支付宝退款接口
                log.info("[PAY] 调用支付宝退款 orderNo={} amount={}", orderNo, amount);
            } else {
                log.info("[PAY-MOCK] 模拟退款 orderNo={} amount={}", orderNo, amount);
            }
            if (userId != null) {
                notificationService.push(userId, "退款通知",
                        String.format("您的订单 %s 已取消，退款 ¥%s 将原路返回。", orderNo, amount));
            }
        }
    }

    private void markFlowPaid(String orderNo, int businessType, BigDecimal amount,
                              String thirdTradeNo, String rawCallback, LocalDateTime now) {
        PaymentFlow flow = paymentFlowMapper.selectOne(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, orderNo)
                .orderByDesc(PaymentFlow::getId)
                .last("limit 1"));
        if (flow == null) {
            flow = new PaymentFlow();
            flow.setBusinessOrderNo(orderNo);
            flow.setBusinessType(businessType);
            flow.setPayMethod(1);
            flow.setActualAmount(amount);
            flow.setPayStatus(1);
            flow.setThirdPartyTradeNo(thirdTradeNo);
            flow.setPaySuccessTime(now);
            flow.setOriginalCallback(rawCallback);
            paymentFlowMapper.insert(flow);
        } else if (flow.getPayStatus() != null && flow.getPayStatus() == 0) {
            PaymentFlow upd = new PaymentFlow();
            upd.setId(flow.getId());
            upd.setPayStatus(1);
            upd.setThirdPartyTradeNo(thirdTradeNo);
            upd.setPaySuccessTime(now);
            upd.setOriginalCallback(rawCallback);
            paymentFlowMapper.updateById(upd);
        }
    }

    /** 统一读取订单关键信息 */
    private OrderView loadOrder(String orderNo, int businessType) {
        OrderView v = new OrderView();
        if (businessType == OrderStatus.BIZ_APPOINTMENT) {
            Appointment a = appointmentMapper.selectOne(
                    Wrappers.<Appointment>lambdaQuery().eq(Appointment::getOrderNo, orderNo));
            if (a == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            v.id = a.getId();
            v.userId = a.getUserId();
            v.amount = a.getAmount();
            v.paid = a.getStatus() != null && a.getStatus() >= OrderStatus.APPT_PAID
                    && a.getStatus() != OrderStatus.APPT_CANCELLED;
            v.cancelled = a.getStatus() != null && a.getStatus() == OrderStatus.APPT_CANCELLED;
        } else if (businessType == OrderStatus.BIZ_CONSULT) {
            Consult c = consultMapper.selectOne(
                    Wrappers.<Consult>lambdaQuery().eq(Consult::getOrderNo, orderNo));
            if (c == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            v.id = c.getId();
            v.userId = c.getUserId();
            v.amount = c.getAmount();
            v.paid = c.getStatus() != null && c.getStatus() >= OrderStatus.CONSULT_PAID
                    && c.getStatus() != OrderStatus.CONSULT_CANCELLED;
            v.cancelled = c.getStatus() != null && c.getStatus() == OrderStatus.CONSULT_CANCELLED;
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "未知业务类型");
        }
        return v;
    }

    /** 订单关键信息内部载体 */
    private static class OrderView {
        Long id;
        Long userId;
        BigDecimal amount;
        boolean paid;
        boolean cancelled;
    }
}
