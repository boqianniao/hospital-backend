package com.hospital.service;

import cn.hutool.core.util.RandomUtil;
import com.hospital.common.constant.RedisKeys;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.config.props.HospitalProperties;
import com.hospital.third.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务：生成 -> 存 Redis(带过期) -> 通过短信服务下发；校验后即删除（一次性）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyCodeService {

    private final StringRedisTemplate redis;
    private final SmsService smsService;
    private final HospitalProperties props;

    /** 发送验证码 */
    public void sendCode(String phone, String scene) {
        String code = RandomUtil.randomNumbers(props.getSms().getCodeLength());
        String key = RedisKeys.smsCode(scene, phone);
        redis.opsForValue().set(key, code, props.getSms().getCodeExpireSeconds(), TimeUnit.SECONDS);
        smsService.sendVerifyCode(phone, code);
    }

    /** 校验验证码；成功后删除。失败抛业务异常 */
    public void verify(String phone, String scene, String inputCode) {
        if (!StringUtils.hasText(inputCode)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        String key = RedisKeys.smsCode(scene, phone);
        String saved = redis.opsForValue().get(key);
        if (saved == null || !saved.equals(inputCode)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        redis.delete(key);
    }
}
