package com.hospital.common.util;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成：前缀 + yyyyMMddHHmmssSSS + 4位随机。
 * GH=挂号 ZX=咨询。
 */
public final class OrderNoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private OrderNoGenerator() {
    }

    public static String appointment() {
        return "GH" + LocalDateTime.now().format(FMT) + RandomUtil.randomNumbers(4);
    }

    public static String consult() {
        return "ZX" + LocalDateTime.now().format(FMT) + RandomUtil.randomNumbers(4);
    }
}
