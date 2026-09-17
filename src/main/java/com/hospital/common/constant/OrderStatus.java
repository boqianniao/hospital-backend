package com.hospital.common.constant;

/**
 * 订单状态常量与文案。
 */
public final class OrderStatus {

    private OrderStatus() {
    }

    // 挂号订单 t_appointment: 1待支付 2已支付 3已完成 4已取消
    public static final int APPT_UNPAID = 1;
    public static final int APPT_PAID = 2;
    public static final int APPT_DONE = 3;
    public static final int APPT_CANCELLED = 4;

    // 咨询订单 t_consult: 1待支付 2已支付 3咨询中 4已完成 5已取消
    public static final int CONSULT_UNPAID = 1;
    public static final int CONSULT_PAID = 2;
    public static final int CONSULT_ING = 3;
    public static final int CONSULT_DONE = 4;
    public static final int CONSULT_CANCELLED = 5;

    // 业务类型
    public static final int BIZ_APPOINTMENT = 1;
    public static final int BIZ_CONSULT = 2;

    public static String appointmentText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    public static String consultText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "咨询中";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}
