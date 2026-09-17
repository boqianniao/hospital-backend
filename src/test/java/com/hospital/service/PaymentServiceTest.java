package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hospital.common.exception.BusinessException;
import com.hospital.config.props.HospitalProperties;
import com.hospital.entity.Appointment;
import com.hospital.entity.PaymentFlow;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.ConsultMapper;
import com.hospital.mapper.PaymentFlowMapper;
import com.hospital.third.pay.AlipayService;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    PaymentFlowMapper flows = mock(PaymentFlowMapper.class);
    AppointmentMapper appointments = mock(AppointmentMapper.class);
    ConsultMapper consults = mock(ConsultMapper.class);
    NotificationService notifications = mock(NotificationService.class);
    AlipayService alipay = mock(AlipayService.class);
    HospitalProperties props = new HospitalProperties();
    PaymentService service;
    Appointment order;
    PaymentFlow flow;

    @BeforeEach void setup() {
        props.getAlipay().setSellerId("seller");
        service = new PaymentService(flows, appointments, consults, notifications, props, alipay);
        order = new Appointment(); order.setId(1L); order.setUserId(7L); order.setOrderNo("GH1");
        order.setAmount(new BigDecimal("10.00")); order.setStatus(1);
        flow = new PaymentFlow(); flow.setId(2L); flow.setBusinessType(1); flow.setBusinessOrderNo("GH1");
        flow.setActualAmount(new BigDecimal("10.00")); flow.setPayMethod(1); flow.setPayStatus(0);
        when(appointments.selectOne(any(Wrapper.class))).thenReturn(order);
        when(flows.selectOne(any(Wrapper.class))).thenReturn(flow);
        when(alipay.isReady()).thenReturn(true);
        when(alipay.verifyNotify(anyMap())).thenReturn(true);
    }
    Map<String, String> callback() {
        return new HashMap<>(Map.of("seller_id", "seller", "out_trade_no", "GH1", "trade_no", "ALI1",
                "trade_status", "TRADE_SUCCESS", "total_amount", "10.0"));
    }
    @Test void paymentFailureNeverFallsBackToMock() {
        when(alipay.pagePay(anyString(), anyString(), anyString(), any())).thenThrow(new BusinessException("unavailable"));
        assertThrows(BusinessException.class, () -> service.createPay(7L, "GH1", 1, 1));
        verify(appointments, never()).updateById(any(Appointment.class));
    }
    @Test void noConfigurationRejectsPayment() {
        when(alipay.isReady()).thenReturn(false);
        assertThrows(BusinessException.class, () -> service.createPay(7L, "GH1", 1, 1));
    }
    @Test void validPagePayReturnsRealForm() {
        when(alipay.pagePay(anyString(), anyString(), anyString(), any())).thenReturn("<form></form>");
        var info = service.createPay(7L, "GH1", 1, 1);
        assertFalse(info.isMock()); assertNull(info.getMockNotifyUrl()); assertEquals("<form></form>", info.getPayForm());
    }
    @Test void unsupportedPaymentMethodRejected() {
        assertThrows(BusinessException.class, () -> service.createPay(7L, "GH1", 1, 2));
        verifyNoInteractions(notifications);
    }
    @Test void badSignatureNeverTouchesOrders() {
        when(alipay.verifyNotify(anyMap())).thenReturn(false);
        assertFalse(service.handleAlipayNotify(callback()));
        verifyNoInteractions(appointments, flows);
    }
    @Test void wrongSellerRejected() {
        var params = callback(); params.put("seller_id", "another");
        assertFalse(service.handleAlipayNotify(params)); verifyNoInteractions(appointments, flows);
    }
    @Test void missingStatusRejected() {
        var params = callback(); params.remove("trade_status");
        assertFalse(service.handleAlipayNotify(params)); verifyNoInteractions(appointments, flows);
    }
    @Test void wrongAmountRejected() {
        var params = callback(); params.put("total_amount", "0.01");
        assertFalse(service.handleAlipayNotify(params));
        verify(flows, never()).updateById(any(PaymentFlow.class));
        verify(appointments, never()).updateById(any(Appointment.class));
    }
    @Test void missingFlowDoesNotCreatePayment() {
        when(flows.selectOne(any(Wrapper.class))).thenReturn(null);
        assertFalse(service.handleAlipayNotify(callback())); verify(flows, never()).insert(any(PaymentFlow.class));
    }
    @Test void callbackMarksOrderAndFlowAndDuplicateDoesNotNotifyAgain() {
        assertTrue(service.handleAlipayNotify(callback()));
        verify(appointments).updateById(argThat((Appointment a) -> a.getStatus() == 2));
        verify(flows).updateById(argThat((PaymentFlow f) -> f.getPayStatus() == 1 && "ALI1".equals(f.getThirdPartyTradeNo())));
        order.setStatus(2); flow.setPayStatus(1); flow.setThirdPartyTradeNo("ALI1");
        assertTrue(service.handleAlipayNotify(callback()));
        verify(notifications, times(1)).push(anyLong(), anyString(), anyString());
        verify(appointments, times(1)).updateById(any(Appointment.class));
    }
    @Test void duplicateWithDifferentTradeNoRejected() {
        order.setStatus(2); flow.setPayStatus(1); flow.setThirdPartyTradeNo("OTHER");
        assertFalse(service.handleAlipayNotify(callback())); verifyNoInteractions(notifications);
    }
    @Test void cancelledLatePaymentRefundsWithoutReopeningOrder() {
        order.setStatus(4); when(alipay.refund("GH1", "10.00")).thenReturn(true);
        assertTrue(service.handleAlipayNotify(callback()));
        verify(appointments, never()).updateById(any(Appointment.class));
        verify(flows).updateById(argThat((PaymentFlow f) -> f.getPayStatus() == 2));
    }
    @Test void failedLateRefundRequestsNotifyRetry() {
        order.setStatus(4);
        assertFalse(service.handleAlipayNotify(callback())); verify(flows, never()).updateById(any(PaymentFlow.class));
    }
    @Test void disabledMockCannotMarkOrderPaid() {
        assertThrows(BusinessException.class, () -> service.mockSuccess(7L, "GH1", 1));
        verifyNoInteractions(appointments, flows);
    }
    @Test void mockChecksOwner() {
        when(alipay.isMockEnabled()).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.mockSuccess(8L, "GH1", 1));
        verify(flows, never()).updateById(any(PaymentFlow.class));
    }
    @Test void queryChecksOwnerBeforeRemoteCall() {
        assertThrows(BusinessException.class, () -> service.queryPay(8L, "GH1", 1));
        verify(alipay, never()).query(anyString());
    }
    @Test void querySynchronizesVerifiedTrade() {
        when(alipay.query("GH1")).thenReturn(new AlipayTradeQueryResponse().setOutTradeNo("GH1")
                .setTradeNo("ALI1").setTradeStatus("TRADE_SUCCESS").setTotalAmount("10.00"));
        assertTrue(service.queryPay(7L, "GH1", 1));
        verify(flows).updateById(argThat((PaymentFlow f) -> f.getPayStatus() == 1));
    }
    @Test void returnMustVerifyBeforeQuery() {
        when(alipay.verifyNotify(anyMap())).thenReturn(false);
        assertFalse(service.handleAlipayReturn(callback()).paid()); verify(alipay, never()).query(anyString());
    }
    @Test void verifiedPaidReturnIncludesSuccessTarget() {
        when(alipay.query("GH1")).thenReturn(new AlipayTradeQueryResponse().setOutTradeNo("GH1")
                .setTradeNo("ALI1").setTradeStatus("TRADE_SUCCESS").setTotalAmount("10.00"));
        var result = service.handleAlipayReturn(callback());
        assertTrue(result.paid());
        assertEquals("GH1", result.orderNo());
        assertEquals(1, result.businessType());
        assertEquals(1L, result.orderId());
    }
    @Test void failedRefundDoesNotMarkRefunded() {
        order.setStatus(2); flow.setPayStatus(1); flow.setThirdPartyTradeNo("ALI1");
        assertThrows(BusinessException.class, () -> service.refund("GH1", 1, new BigDecimal("10.00"), 7L));
        verify(flows, never()).updateById(any(PaymentFlow.class)); verifyNoInteractions(notifications);
    }
    @Test void successfulRefundMarksFlowAfterRemoteSuccess() {
        order.setStatus(2); flow.setPayStatus(1); flow.setThirdPartyTradeNo("ALI1");
        when(alipay.refund("GH1", "10.00")).thenReturn(true);
        service.refund("GH1", 1, new BigDecimal("10.00"), 7L);
        var sequence = inOrder(alipay, flows);
        sequence.verify(alipay).refund("GH1", "10.00");
        sequence.verify(flows).updateById(argThat((PaymentFlow f) -> f.getPayStatus() == 2));
    }
}
