package com.hospital.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务自定义配置，绑定 application.yml 中的 hospital.* 。
 */
@Data
@Component
@ConfigurationProperties(prefix = "hospital")
public class HospitalProperties {

    private Jwt jwt = new Jwt();
    private Sms sms = new Sms();
    private Alipay alipay = new Alipay();
    private Oss oss = new Oss();
    private Order order = new Order();

    @Data
    public static class Jwt {
        private String secret;
        private long expireSeconds = 604800;
        private String header = "token";
    }

    @Data
    public static class Sms {
        private boolean enabled = false;
        private int codeExpireSeconds = 300;
        private int codeLength = 6;
    }

    @Data
    public static class Alipay {
        private boolean enabled = false;
        private String gateway;
        private String appId;
        private String appPrivateKey;
        private String alipayPublicKey;
        private String notifyUrl;
        private String returnUrl;
    }

    @Data
    public static class Oss {
        private boolean enabled = false;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }

    @Data
    public static class Order {
        private int timeoutMinutes = 15;
    }
}
