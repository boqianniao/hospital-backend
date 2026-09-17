package com.hospital.service;

import com.hospital.common.constant.RedisKeys;
import com.hospital.common.constant.SmsScene;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.dto.auth.LoginDTO;
import com.hospital.dto.auth.RegisterDTO;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import com.hospital.common.util.JwtUtil;
import com.hospital.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务：注册 / 登录 / 登出。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final VerifyCodeService verifyCodeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final org.springframework.data.redis.core.StringRedisTemplate redis;

    /** 注册 */
    public void register(RegisterDTO dto) {
        // 1. 校验验证码
        verifyCodeService.verify(dto.getPhone(), SmsScene.REGISTER, dto.getCode());
        // 2. 手机号唯一
        if (userService.getByPhone(dto.getPhone()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // 3. 用户名：缺省用手机号，若指定需唯一
        String username = StringUtils.hasText(dto.getUsername()) ? dto.getUsername() : dto.getPhone();
        if (userService.getByUsername(username) != null) {
            throw new BusinessException("该用户名已被占用");
        }
        // 4. 落库（密码 BCrypt 加密）
        User user = new User();
        user.setUsername(username);
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setAvatar("img/default-avatar.png");
        userMapper.insert(user);
    }

    /** 登录：校验密码 -> 生成 token -> 写 Redis */
    public LoginVO login(LoginDTO dto) {
        User user = userService.getByPhone(dto.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        String token = jwtUtil.generate(user.getId(), user.getPhone());
        redis.opsForValue().set(RedisKeys.loginToken(token), String.valueOf(user.getId()),
                jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(userService.toVO(user));
        return vo;
    }

    /** 登出：删除 Redis token */
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            redis.delete(RedisKeys.loginToken(token));
        }
    }
}
