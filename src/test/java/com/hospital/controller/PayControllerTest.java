package com.hospital.controller;

import com.hospital.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PayControllerTest {
    PaymentService payments = mock(PaymentService.class);
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new PayController(payments)).build();
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
    @Test void returnDoesNotEchoUntrustedHtml() throws Exception {
        mvc.perform(get("/api/pay/alipay/return").param("out_trade_no", "<script>alert(1)</script>"))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))));
    }
}
