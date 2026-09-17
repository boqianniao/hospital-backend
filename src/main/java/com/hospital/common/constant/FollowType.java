package com.hospital.common.constant;

/**
 * 关注类型：1医院 2医生 3疾病。
 */
public final class FollowType {

    private FollowType() {
    }

    public static final int HOSPITAL = 1;
    public static final int DOCTOR = 2;
    public static final int DISEASE = 3;

    public static boolean valid(Integer type) {
        return type != null && type >= 1 && type <= 3;
    }
}
