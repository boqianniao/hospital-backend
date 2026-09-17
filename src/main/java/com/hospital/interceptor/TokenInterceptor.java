package com.hospital.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.common.constant.RedisKeys;
import com.hospital.common.context.UserContext;
import com.hospital.common.result.Result;
import com.hospital.common.result.ResultCode;
import com.hospital.common.util.JwtUtil;
import com.hospital.config.props.HospitalProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录鉴权拦截器：
 * 1. 从请求头取 token（头名可配，默认 token，与前端约定一致）；
 * 2. 校验 JWT 签名/过期，并确认 Redis 中仍存在（支持登出吊销）；
 * 3. 校验通过写入 UserContext，否则返回 code=30001 让前端跳登录。
 */
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final HospitalProperties props;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(props.getJwt().getHeader());
        if (!StringUtils.hasText(token)) {
            token = request.getHeader("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response);
            return false;
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            writeUnauthorized(response);
            return false;
        }
        // Redis 中不存在说明已登出/被吊销
        String cached = redis.opsForValue().get(RedisKeys.loginToken(token));
        if (cached == null) {
            writeUnauthorized(response);
            return false;
        }
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> body = Result.fail(ResultCode.TOKEN_INVALID);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
