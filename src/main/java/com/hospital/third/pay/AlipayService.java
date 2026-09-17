package com.hospital.third.pay;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.config.props.HospitalProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/** 支付宝 RSA2 公钥模式接入。启用失败时拒绝支付，不降级到模拟支付。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {
    private final HospitalProperties props;
    private volatile boolean ready;

    @PostConstruct
    public void init() {
        ready = false;
        var cfg = props.getAlipay();
        if (!cfg.isEnabled()) return;
        if (!StringUtils.hasText(cfg.getAppId()) || !StringUtils.hasText(cfg.getSellerId())
                || !StringUtils.hasText(cfg.getAppPrivateKey()) || !StringUtils.hasText(cfg.getAlipayPublicKey())) {
            throw new IllegalStateException("支付宝已启用，请配置 app-id、seller-id、app-private-key、alipay-public-key");
        }
        URI gateway = httpUrl(cfg.getGateway(), "gateway");
        if (!"https".equals(gateway.getScheme())) throw new IllegalStateException("支付宝网关必须使用 HTTPS");
        httpUrl(cfg.getReturnUrl(), "return-url");
        if (StringUtils.hasText(cfg.getNotifyUrl())) httpUrl(cfg.getNotifyUrl(), "notify-url");
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = gateway.getHost();
        config.signType = "RSA2";
        config.appId = cfg.getAppId();
        config.merchantPrivateKey = normalizeKey(cfg.getAppPrivateKey());
        config.alipayPublicKey = normalizeKey(cfg.getAlipayPublicKey());
        config.notifyUrl = StringUtils.hasText(cfg.getNotifyUrl()) ? cfg.getNotifyUrl() : null;
        try {
            KeyFactory keys = KeyFactory.getInstance("RSA");
            keys.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(config.merchantPrivateKey)));
            keys.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(config.alipayPublicKey)));
            Factory.setOptions(config);
        } catch (Exception e) {
            throw new IllegalStateException("支付宝 RSA2 密钥格式错误，请使用 JAVA/PKCS8 应用私钥和支付宝公钥");
        }
        ready = true;
        log.info("[ALIPAY] 初始化完成 gatewayHost={} appId={}", config.gatewayHost, config.appId);
    }

    public boolean isReady() { return ready; }

    public boolean isMockEnabled() {
        return !props.getAlipay().isEnabled() && props.getAlipay().isMockEnabled();
    }

    public String pagePay(String subject, String outTradeNo, String totalAmount, String returnUrl) {
        requireReady();
        try {
            String body = Factory.Payment.Page()
                    .optional("timeout_express", props.getOrder().getTimeoutMinutes() + "m")
                    .pay(subject, outTradeNo, totalAmount, returnUrl).getBody();
            if (!StringUtils.hasText(body)) throw new IllegalStateException("empty response");
            return body;
        } catch (Exception e) {
            log.warn("[ALIPAY] 网页支付生成失败 type={}", e.getClass().getSimpleName());
            throw new BusinessException(ResultCode.PAY_ERROR, "支付宝下单失败，请稍后重试");
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        if (!ready || !"RSA2".equals(params.get("sign_type")) || !StringUtils.hasText(params.get("sign"))
                || !props.getAlipay().getAppId().equals(params.get("app_id"))) return false;
        try {
            return Boolean.TRUE.equals(Factory.Payment.Common().verifyNotify(params));
        } catch (Exception e) {
            return false;
        }
    }

    /** 查单响应由 SDK 验签，交易不存在返回未支付，其他业务错误不当成未支付。 */
    public AlipayTradeQueryResponse query(String orderNo) {
        requireReady();
        try {
            var response = Factory.Payment.Common().query(orderNo);
            if ("40004".equals(response.getCode()) && "ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) return null;
            if (!"10000".equals(response.getCode()) || !orderNo.equals(response.getOutTradeNo())) {
                throw new IllegalStateException("query rejected");
            }
            return response;
        } catch (Exception e) {
            log.warn("[ALIPAY] 查询失败 type={}", e.getClass().getSimpleName());
            throw new BusinessException(ResultCode.PAY_ERROR, "支付宝查单失败，请稍后重试");
        }
    }

    public boolean refund(String outTradeNo, String refundAmount) {
        requireReady();
        try {
            var response = Factory.Payment.Common().optional("out_request_no", "REFUND_" + outTradeNo)
                    .refund(outTradeNo, refundAmount);
            return "10000".equals(response.getCode());
        } catch (Exception e) {
            log.warn("[ALIPAY] 退款失败 type={}", e.getClass().getSimpleName());
            return false;
        }
    }

    private void requireReady() {
        if (!ready) throw new BusinessException(ResultCode.PAY_ERROR, "支付宝支付未启用");
    }

    private static String normalizeKey(String key) {
        return key.replaceAll("-----BEGIN [A-Z ]+-----|-----END [A-Z ]+-----|\\s", "");
    }

    private static URI httpUrl(String value, String field) {
        try {
            URI uri = URI.create(value);
            if (uri.getHost() != null && uri.getUserInfo() == null
                    && ("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))) return uri;
        } catch (Exception ignored) { }
        throw new IllegalStateException("支付宝 " + field + " 必须是完整的 HTTP(S) 地址");
    }
}
