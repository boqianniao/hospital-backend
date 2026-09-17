package com.hospital.third.sms;

import com.hospital.config.props.HospitalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务桩实现：未接入真实短信通道时，把验证码打印到日志，便于本地联调。
 * 接入真实短信只需替换本实现或新增一个实现并调整为 @Primary。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final HospitalProperties props;

    @Override
    public void sendVerifyCode(String phone, String code) {
        if (props.getSms().isEnabled()) {
            // TODO: 接入阿里云/腾讯云短信 SDK
            log.info("[SMS] 调用真实短信通道发送验证码 phone={} code={}", phone, code);
        } else {
            log.info("[SMS-MOCK] 向 {} 发送验证码: {} （桩实现，未真实发送）", phone, code);
        }
    }
}
