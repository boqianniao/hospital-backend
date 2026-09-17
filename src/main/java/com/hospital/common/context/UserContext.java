package com.hospital.common.context;

/**
 * 基于 ThreadLocal 的当前登录用户上下文，由 TokenInterceptor 写入，请求结束时清理。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /** 当前登录用户 id；未登录返回 null */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 要求已登录，否则抛异常，供业务层强制取用户 id */
    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new com.hospital.common.exception.BusinessException(
                    com.hospital.common.result.ResultCode.UNAUTHORIZED);
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
