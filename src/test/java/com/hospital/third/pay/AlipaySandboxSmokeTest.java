package com.hospital.third.pay;

import com.hospital.config.props.HospitalProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 显式运行；仅查询一个不存在的沙箱订单，不创建交易或修改业务库。 */
@EnabledIfSystemProperty(named = "alipay.sandbox.smoke", matches = "true")
class AlipaySandboxSmokeTest {
    @Test void sandboxCredentialsCanSignAndCallGateway() throws Exception {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application.yml"));
        var application = yaml.getObject();
        assertNotNull(application);
        var props = new HospitalProperties();
        var cfg = props.getAlipay();
        cfg.setEnabled(true);
        cfg.setAppId(application.getProperty("hospital.alipay.app-id"));
        cfg.setSellerId(application.getProperty("hospital.alipay.seller-id"));
        cfg.setAppPrivateKey(application.getProperty("hospital.alipay.app-private-key"));
        cfg.setAlipayPublicKey(application.getProperty("hospital.alipay.alipay-public-key"));
        cfg.setGateway(application.getProperty("hospital.alipay.gateway"));
        cfg.setReturnUrl("http://localhost:8080/api/pay/alipay/return");
        assertEquals("https://openapi-sandbox.dl.alipaydev.com/gateway.do", cfg.getGateway());
        var service = new AlipayService(props);
        service.init();
        String orderNo = "CHECK" + UUID.randomUUID().toString().replace("-", "");
        String form = service.pagePay("sandbox-connection-check", orderNo, "0.01", cfg.getReturnUrl());
        assertTrue(form.contains("alipay.trade.page.pay"));
        assertNull(service.query(orderNo));
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/alipay-sandbox-form.html"), form);
        System.out.println("Sandbox gateway accepted signed query; test trade does not exist; payment form generated.");
    }
}
