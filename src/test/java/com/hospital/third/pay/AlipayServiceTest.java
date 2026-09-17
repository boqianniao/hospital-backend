package com.hospital.third.pay;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.Client;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.hospital.common.exception.BusinessException;
import com.hospital.config.props.HospitalProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlipayServiceTest {
    HospitalProperties props;
    AlipayService service;
    KeyPair pair;
    @BeforeEach void setup() throws Exception {
        pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        props = new HospitalProperties();
        var c = props.getAlipay(); c.setEnabled(true); c.setAppId("app"); c.setSellerId("seller");
        c.setGateway("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        c.setReturnUrl("http://localhost:8080/api/pay/alipay/return");
        c.setAppPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        c.setAlipayPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        service = new AlipayService(props); service.init();
    }
    @Test void enabledMissingCredentialsFailsFast() {
        props.getAlipay().setAppPrivateKey("");
        assertThrows(IllegalStateException.class, service::init); assertFalse(service.isReady());
    }
    @Test void invalidKeyFailsFast() {
        props.getAlipay().setAppPrivateKey("not-a-key"); assertThrows(IllegalStateException.class, service::init);
    }
    @Test void mockRequiresExplicitFlagAndDisabledAlipay() {
        props.getAlipay().setMockEnabled(true); assertFalse(service.isMockEnabled());
        props.getAlipay().setEnabled(false); service.init(); assertTrue(service.isMockEnabled());
        assertFalse(service.verifyNotify(Map.of()));
    }
    @Test void pagePayUsesSandboxGatewayAndRsa2() {
        String html = service.pagePay("test", "GH1", "1.00", props.getAlipay().getReturnUrl());
        assertTrue(html.contains("https://openapi-sandbox.dl.alipaydev.com/gateway.do"));
        assertTrue(html.contains("RSA2")); assertTrue(html.contains("alipay.trade.page.pay"));
        assertFalse(html.contains("notify_url")); assertTrue(html.contains("15m"));
    }
    @Test void rsa2NotificationAcceptsValidSignatureRejectsTamperingAndWrongApp() throws Exception {
        Map<String,String> params = new HashMap<>(Map.of("app_id", "app", "out_trade_no", "GH1", "total_amount", "1.00"));
        String content = new TreeMap<>(params).entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("&"));
        Signature signature = Signature.getInstance("SHA256withRSA"); signature.initSign(pair.getPrivate());
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        params.put("sign", Base64.getEncoder().encodeToString(signature.sign())); params.put("sign_type", "RSA2");
        assertTrue(service.verifyNotify(params));
        params.put("total_amount", "100.00"); assertFalse(service.verifyNotify(params));
        params.put("total_amount", "1.00"); params.put("app_id", "other"); assertFalse(service.verifyNotify(params));
    }
    @Test void businessRefundFailureIsNotSuccess() throws Exception {
        Client client = mock(Client.class); when(client.optional(anyString(), any())).thenReturn(client);
        when(client.refund("GH1", "1.00")).thenReturn(new AlipayTradeRefundResponse().setCode("40004"));
        try (var mocked = mockStatic(Factory.Payment.class)) {
            mocked.when(Factory.Payment::Common).thenReturn(client);
            assertFalse(service.refund("GH1", "1.00"));
            verify(client).optional("out_request_no", "REFUND_GH1");
        }
    }
    @Test void tradeNotFoundIsPendingButGatewayErrorsAreFailures() throws Exception {
        Client client = mock(Client.class);
        when(client.query("GH1")).thenReturn(new AlipayTradeQueryResponse().setCode("40004").setSubCode("ACQ.TRADE_NOT_EXIST"),
                new AlipayTradeQueryResponse().setCode("40002").setSubCode("isv.invalid-signature"));
        try (var mocked = mockStatic(Factory.Payment.class)) {
            mocked.when(Factory.Payment::Common).thenReturn(client);
            assertNull(service.query("GH1")); assertThrows(BusinessException.class, () -> service.query("GH1"));
        }
    }
}
