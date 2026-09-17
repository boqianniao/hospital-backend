package com.hospital.service;

import com.hospital.common.constant.RedisKeys;
import com.hospital.config.props.HospitalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 搜索历史（每用户 List）与热门搜索（全局 ZSet，score=搜索次数）。
 * Redis 不可用时静默降级，不影响主搜索流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final StringRedisTemplate redis;
    private final HospitalProperties props;

    /** 记录一次用户搜索：去重置顶、截断、续期 */
    public void addHistory(Long userId, String keyword) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        String key = RedisKeys.searchHistory(userId);
        try {
            redis.opsForList().remove(key, 0, kw);
            redis.opsForList().leftPush(key, kw);
            redis.opsForList().trim(key, 0, props.getSearch().getHistoryMax() - 1);
            redis.expire(key, props.getSearch().getHistoryTtlDays(), TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录搜索历史失败 userId={} kw={}: {}", userId, kw, e.getMessage());
        }
    }

    /** 我的搜索历史（最近在前） */
    public List<String> listHistory(Long userId) {
        try {
            List<String> list = redis.opsForList().range(RedisKeys.searchHistory(userId), 0, props.getSearch().getHistoryMax() - 1);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            log.warn("读取搜索历史失败 userId={}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 清空我的搜索历史 */
    public void clearHistory(Long userId) {
        try {
            redis.delete(RedisKeys.searchHistory(userId));
        } catch (Exception e) {
            log.warn("清空搜索历史失败 userId={}: {}", userId, e.getMessage());
        }
    }

    /** 关键词热度 +1 */
    public void incrHot(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        try {
            redis.opsForZSet().incrementScore(RedisKeys.SEARCH_HOT, keyword.trim(), 1);
        } catch (Exception e) {
            log.warn("热搜计数失败 kw={}: {}", keyword, e.getMessage());
        }
    }

    /** 热门搜索词（按次数降序） */
    public List<String> hotKeywords(Integer limit) {
        HospitalProperties.Search cfg = props.getSearch();
        int n = (limit == null || limit <= 0) ? cfg.getHotDefaultLimit() : Math.min(limit, cfg.getHotMaxLimit());
        try {
            Set<String> set = redis.opsForZSet().reverseRange(RedisKeys.SEARCH_HOT, 0, n - 1);
            return (set == null || set.isEmpty()) ? Collections.emptyList() : List.copyOf(set);
        } catch (Exception e) {
            log.warn("读取热搜失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
