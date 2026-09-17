package com.hospital.third.pay;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.hospital.config.props.HospitalProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Map;

/**
 * 支付宝沙箱封装（EasySDK）。
 * 仅当 hospital.alipay.enabled=true 且 appId/应用私钥/支付宝公钥齐全时才初始化；
 * 否则 isReady()=false，上层支付逻辑走本地支付桩，保证凭证留空时应用照常启动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final HospitalProperties props;

    /** 是否已完成 SDK 初始化并可发起真实支付宝调用 */
    private volatile boolean ready = false;

    @PostConstruct
    public void init() {
        HospitalProperties.Alipay cfg = props.getAlipay();
        if (!cfg.isEnabled()) {
            log.info("[ALIPAY] 未启用，支付走本地桩（hospital.alipay.enabled=false）");
            return;
        }
        if (!StringUtils.hasText(cfg.getAppId())
                || !StringUtils.hasText(cfg.getAppPrivateKey())
                || !StringUtils.hasText(cfg.getAlipayPublicKey())) {
            log.warn("[ALIPAY] 已启用但凭证不完整，降级为支付桩。请补齐 app-id / app-private-key / alipay-public-key");
            return;
        }
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = resolveHost(cfg.getGateway());
        config.signType = "RSA2";
        config.appId = cfg.getAppId();
        config.merchantPrivateKey = cfg.getAppPrivateKey();
        config.alipayPublicKey = cfg.getAlipayPublicKey();
        config.notifyUrl = cfg.getNotifyUrl();
        Factory.setOptions(config);
        ready = true;
        log.info("[ALIPAY] 沙箱初始化完成 gatewayHost={} appId={}", config.gatewayHost, config.appId);
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * PC 网页支付：返回自动提交的 HTML 表单，前端直接写入页面即可跳转收银台。
     * 失败返回 null，由上层降级处理。
     */
    public String pagePay(String subject, String outTradeNo, String totalAmount, String returnUrl) {
        try {
            return Factory.Payment.Page().pay(subject, outTradeNo, totalAmount, returnUrl).getBody();
        } catch (Exception e) {
            log.error("[ALIPAY] 网页下单失败 outTradeNo={}: {}", outTradeNo, e.getMessage(), e);
            return null;
        }
    }

    /** 异步回调验签 */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return Boolean.TRUE.equals(Factory.Payment.Common().verifyNotify(params));
        } catch (Exception e) {
            log.error("[ALIPAY] 回调验签异常: {}", e.getMessage());
            return false;
        }
    }

    /** 退款；成功返回 true */
    public boolean refund(String outTradeNo, String refundAmount) {
        try {
            Factory.Payment.Common().refund(outTradeNo, refundAmount);
            return true;
        } catch (Exception e) {
            log.error("[ALIPAY] 退款失败 outTradeNo={}: {}", outTradeNo, e.getMessage());
            return false;
        }
    }

    /** EasySDK 的 gatewayHost 只需主机名；兼容配置里写完整 URL 的情况 */
    private String resolveHost(String gateway) {
        if (!StringUtils.hasText(gateway)) {
            return "openapi.alipay.com";
        }
        try {
            if (gateway.contains("://")) {
                String host = URI.create(gateway).getHost();
                return StringUtils.hasText(host) ? host : gateway;
            }
        } catch (Exception ignore) {
            // 配置非标准 URL，原样使用
        }
        return gateway;
    }
}
