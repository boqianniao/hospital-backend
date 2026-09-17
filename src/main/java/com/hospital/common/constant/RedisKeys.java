package com.hospital.common.constant;

/**
 * Redis Key 统一定义，避免散落魔法字符串。
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 登录 token -> userId，用于服务端校验与登出吊销。key: login:token:{token} */
    public static final String LOGIN_TOKEN = "login:token:";

    /** 短信/注册验证码。key: sms:code:{scene}:{phone} */
    public static final String SMS_CODE = "sms:code:";

    /** 医院详情缓存。key: cache:hospital:{id} */
    public static final String CACHE_HOSPITAL = "cache:hospital:";

    /** 医生详情缓存。key: cache:doctor:{id} */
    public static final String CACHE_DOCTOR = "cache:doctor:";

    /** 号源剩余数（原子扣减）。key: schedule:stock:{scheduleId} */
    public static final String SCHEDULE_STOCK = "schedule:stock:";

    /** 热门搜索词（ZSet，score=搜索次数）。key: search:hot */
    public static final String SEARCH_HOT = "search:hot";

    /** 用户搜索历史（List）。key: search:history:{userId} */
    public static final String SEARCH_HISTORY = "search:history:";

    public static String loginToken(String token) {
        return LOGIN_TOKEN + token;
    }

    public static String smsCode(String scene, String phone) {
        return SMS_CODE + scene + ":" + phone;
    }

    public static String hospital(Long id) {
        return CACHE_HOSPITAL + id;
    }

    public static String doctor(Long id) {
        return CACHE_DOCTOR + id;
    }

    public static String scheduleStock(Long scheduleId) {
        return SCHEDULE_STOCK + scheduleId;
    }

    public static String searchHistory(Long userId) {
        return SEARCH_HISTORY + userId;
    }
}
