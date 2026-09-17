package com.hospital.service;

import com.hospital.config.props.HospitalProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 通用缓存助手（基于 RedisTemplate JSON 序列化），提供 cache-aside 读取。
 * ES/Redis 不可用时不影响主流程：读缓存异常直接回源 DB。
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HospitalProperties props;

    /**
     * cache-aside：先读缓存，命中直接返回；未命中回源并写缓存。
     * @param key    缓存 key
     * @param type   目标类型
     * @param loader 回源函数
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (T) cached;
            }
        } catch (Exception ignore) {
            // 缓存不可用则回源
        }
        T value = loader.get();
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(key, value, props.getCache().getDetailTtlSeconds(), TimeUnit.SECONDS);
            } catch (Exception ignore) {
                // 写缓存失败不影响返回
            }
        }
        return value;
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignore) {
        }
    }
}
