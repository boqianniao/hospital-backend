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
    private Cache cache = new Cache();
    private Search search = new Search();
    private String frontendBaseUrl = "http://localhost:5500";

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
        /** 阿里云号码认证服务（dypnsapi）配置 */
        private String accessKeyId;
        private String accessKeySecret;
        private String endpoint = "dypnsapi.aliyuncs.com";
        /** 短信签名名称（控制台审核通过） */
        private String signName;
        /** 短信模板 CODE（控制台审核通过） */
        private String templateCode;
    }

    @Data
    public static class Alipay {
        private boolean enabled = false;
        private boolean mockEnabled = false;
        private String sellerId;
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
        /** 超时订单扫描周期（毫秒），OrderTimeoutTask 轮询间隔 */
        private long timeoutScanMs = 60000;
    }

    @Data
    public static class Cache {
        /** 详情类缓存默认时长（秒） */
        private long detailTtlSeconds = 1800;
    }

    @Data
    public static class Search {
        /** ES 判定不可用后重新探活的最小间隔（毫秒） */
        private long esRetryIntervalMs = 60000;
        /** 每个用户保留的搜索历史条数上限 */
        private int historyMax = 20;
        /** 搜索历史过期天数 */
        private long historyTtlDays = 30;
        /** 热搜默认返回条数 */
        private int hotDefaultLimit = 10;
        /** 热搜单次返回条数上限 */
        private int hotMaxLimit = 50;
    }
}
