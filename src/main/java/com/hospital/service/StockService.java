package com.hospital.service;

import com.hospital.common.constant.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 号源库存（Redis 预扣）。
 * 说明：Redis 承担高并发下的快速预扣与防超卖，DB 的 remain_count 仍是最终事实
 * （见 ScheduleMapper.deductStock），二者配合，出现漂移时以 DB 为准并回补 Redis。
 */
@Slf4j
@Service
public class StockService {

    private final StringRedisTemplate redis;

    /** 原子：若库存>0 则扣减并返回剩余，否则返回 -1 */
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT = new DefaultRedisScript<>(
            "local v = redis.call('get', KEYS[1]) " +
            "if v == false then return -2 end " +
            "if tonumber(v) <= 0 then return -1 end " +
            "return redis.call('decr', KEYS[1])",
            Long.class);

    public StockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 用 DB 剩余号源初始化 Redis 库存（仅当 key 不存在时） */
    public void initIfAbsent(Long scheduleId, int dbRemain) {
        try {
            redis.opsForValue().setIfAbsent(RedisKeys.scheduleStock(scheduleId), String.valueOf(Math.max(dbRemain, 0)));
        } catch (Exception e) {
            log.warn("初始化号源库存失败 scheduleId={}: {}", scheduleId, e.getMessage());
        }
    }

    /**
     * 尝试预扣一个号源。
     * @return true 预扣成功；false 库存不足
     */
    public boolean tryDeduct(Long scheduleId, int dbRemain) {
        String key = RedisKeys.scheduleStock(scheduleId);
        initIfAbsent(scheduleId, dbRemain);
        Long r = redis.execute(DEDUCT_SCRIPT, Collections.singletonList(key));
        if (r != null && r == -2L) {
            // 极端并发下 key 恰好过期/被清，重建后再扣一次
            redis.opsForValue().set(key, String.valueOf(Math.max(dbRemain, 0)));
            r = redis.execute(DEDUCT_SCRIPT, Collections.singletonList(key));
        }
        return r != null && r >= 0L;
    }

    /** 回补一个号源（取消/超时/DB 扣减失败时） */
    public void restore(Long scheduleId) {
        try {
            redis.opsForValue().increment(RedisKeys.scheduleStock(scheduleId));
        } catch (Exception e) {
            log.warn("回补号源库存失败 scheduleId={}: {}", scheduleId, e.getMessage());
        }
    }
}
