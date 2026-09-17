package com.hospital.third.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.hospital.common.exception.BusinessException;
import com.hospital.config.props.HospitalProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 短信服务：阿里云号码认证服务（dypnsapi）SendSmsVerifyCode 下发验证码。
 * 仅当 hospital.sms.enabled=true 且 AccessKey/签名/模板齐全时初始化真实通道；
 * 否则回退为日志桩（打印验证码，便于本地联调），保证凭证缺失时应用照常运行。
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final HospitalProperties props;
    private Client client;
    private volatile boolean ready = false;

    public SmsServiceImpl(HospitalProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        HospitalProperties.Sms cfg = props.getSms();
        if (!cfg.isEnabled()) {
            log.info("[SMS] 未启用，验证码走日志桩（hospital.sms.enabled=false）");
            return;
        }
        if (!StringUtils.hasText(cfg.getAccessKeyId()) || !StringUtils.hasText(cfg.getAccessKeySecret())
                || !StringUtils.hasText(cfg.getSignName()) || !StringUtils.hasText(cfg.getTemplateCode())) {
            log.warn("[SMS] 已启用但配置不完整（AccessKey/签名/模板），降级为日志桩");
            return;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(cfg.getAccessKeyId())
                    .setAccessKeySecret(cfg.getAccessKeySecret());
            config.endpoint = StringUtils.hasText(cfg.getEndpoint()) ? cfg.getEndpoint() : "dypnsapi.aliyuncs.com";
            this.client = new Client(config);
            this.ready = true;
            log.info("[SMS] 阿里云短信初始化完成 endpoint={} sign={} template={}",
                    config.endpoint, cfg.getSignName(), cfg.getTemplateCode());
        } catch (Exception e) {
            log.error("[SMS] 阿里云短信初始化失败，降级为日志桩: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendVerifyCode(String phone, String code) {
        if (!ready) {
            log.info("[SMS-MOCK] 向 {} 发送验证码: {} （桩实现，未真实发送）", phone, code);
            return;
        }
        HospitalProperties.Sms cfg = props.getSms();
        int minutes = Math.max(1, cfg.getCodeExpireSeconds() / 60);
        String templateParam = "{\"code\":\"" + code + "\",\"min\":\"" + minutes + "\"}";
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(cfg.getSignName())
                    .setTemplateCode(cfg.getTemplateCode())
                    .setTemplateParam(templateParam);
            SendSmsVerifyCodeResponse resp = client.sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            SendSmsVerifyCodeResponseBody body = resp.getBody();
            if (body != null && (Boolean.TRUE.equals(body.getSuccess()) || "OK".equalsIgnoreCase(body.getCode()))) {
                log.info("[SMS] 验证码已发送 phone={} code={}", phone, body.getCode());
            } else {
                String detail = body == null ? "空响应" : body.getCode() + "/" + body.getMessage();
                log.error("[SMS] 验证码发送失败 phone={} resp={}", phone, detail);
                throw new BusinessException("验证码发送失败，请稍后重试");
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[SMS] 验证码发送异常 phone={}: {}", phone, e.getMessage(), e);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
    }
}
