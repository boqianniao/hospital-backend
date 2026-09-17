package com.hospital.controller;

import com.hospital.service.PaymentService;
import com.hospital.service.PaymentService.AlipayReturnResult;
import com.hospital.config.props.HospitalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PayControllerTest {
    PaymentService payments = mock(PaymentService.class);
    HospitalProperties properties = properties();
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new PayController(payments, properties)).build();
    @Test void notifyReturnsPlainSuccess() throws Exception {
        when(payments.handleAlipayNotify(anyMap())).thenReturn(true);
        mvc.perform(post("/api/pay/alipay/notify").param("out_trade_no", "GH1"))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("success"));
    }
    @Test void duplicateParameterFailsClosed() throws Exception {
        mvc.perform(post("/api/pay/alipay/notify").param("total_amount", "1.00", "100.00"))
                .andExpect(content().string("failure")); verifyNoInteractions(payments);
    }
    @Test void processingFailureAllowsAlipayRetry() throws Exception {
        when(payments.handleAlipayNotify(anyMap())).thenThrow(new IllegalStateException());
        mvc.perform(post("/api/pay/alipay/notify")).andExpect(content().string("failure"));
    }
    @Test void successfulConsultReturnRedirectsToConsultSuccess() throws Exception {
        when(payments.handleAlipayReturn(anyMap()))
                .thenReturn(new AlipayReturnResult(true, "ZX20260917001", 2, 99L));
        mvc.perform(get("/api/pay/alipay/return").param("out_trade_no", "ZX20260917001"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5500/consult-success.html?id=99&orderNo=ZX20260917001&payMethod=%E6%94%AF%E4%BB%98%E5%AE%9D"));
    }
    @Test void successfulAppointmentReturnRedirectsToAppointmentSuccess() throws Exception {
        when(payments.handleAlipayReturn(anyMap()))
                .thenReturn(new AlipayReturnResult(true, "GH20260917001", 1, 88L));
        mvc.perform(get("/api/pay/alipay/return").param("out_trade_no", "GH20260917001"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5500/reservation-success.html?id=88&orderNo=GH20260917001&payMethod=%E6%94%AF%E4%BB%98%E5%AE%9D"));
    }
    @Test void pendingAppointmentReturnRedirectsToAppointmentOrders() throws Exception {
        when(payments.handleAlipayReturn(anyMap()))
                .thenReturn(new AlipayReturnResult(false, "GH20260917001", 1, 88L));
        mvc.perform(get("/api/pay/alipay/return").param("out_trade_no", "GH20260917001"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5500/my-appointment.html?payment=pending&orderNo=GH20260917001"));
    }
    @Test void invalidReturnDoesNotEchoUntrustedOrderNumber() throws Exception {
        when(payments.handleAlipayReturn(anyMap())).thenReturn(AlipayReturnResult.unverified());
        mvc.perform(get("/api/pay/alipay/return").param("out_trade_no", "<script>alert(1)</script>"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5500/index.html?payment=pending"));
    }

    private static HospitalProperties properties() {
        HospitalProperties properties = new HospitalProperties();
        properties.setFrontendBaseUrl("http://localhost:5500");
        return properties;
    }
}
