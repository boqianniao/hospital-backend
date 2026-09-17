package com.hospital.common.util;

import com.hospital.config.props.HospitalProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析工具（jjwt 0.12.x，HS256）。
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_PHONE = "phone";

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(HospitalProperties props) {
        byte[] secretBytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expireMillis = props.getJwt().getExpireSeconds() * 1000L;
    }

    /** 生成 token */
    public String generate(Long userId, String phone) {
        Date now = new Date();
        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_PHONE, phone)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 解析并校验签名与过期；失败返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parse(token);
        if (claims == null) {
            return null;
        }
        Number userId = claims.get(CLAIM_USER_ID, Number.class);
        return userId == null ? null : userId.longValue();
    }

    public long getExpireSeconds() {
        return expireMillis / 1000L;
    }
}
