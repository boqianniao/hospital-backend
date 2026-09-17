package com.hospital.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OrderNoGenerator 纯逻辑单测：前缀、长度、格式。
 */
class OrderNoGeneratorTest {

    @Test
    void appointment_shouldStartWithGH_andHaveExpectedLength() {
        String no = OrderNoGenerator.appointment();
        assertTrue(no.startsWith("GH"), "挂号订单号应以 GH 开头");
        // 前缀2 + yyyyMMddHHmmssSSS(17) + 4位随机 = 23
        assertEquals(23, no.length());
        assertTrue(no.substring(2).matches("\\d+"), "GH 之后应全为数字");
    }

    @Test
    void consult_shouldStartWithZX_andHaveExpectedLength() {
        String no = OrderNoGenerator.consult();
        assertTrue(no.startsWith("ZX"), "咨询订单号应以 ZX 开头");
        assertEquals(23, no.length());
        assertTrue(no.substring(2).matches("\\d+"), "ZX 之后应全为数字");
    }

    @Test
    void appointmentAndConsult_shouldUseDifferentPrefixes() {
        assertNotEquals(OrderNoGenerator.appointment().substring(0, 2),
                OrderNoGenerator.consult().substring(0, 2), "挂号与咨询前缀应不同");
    }
}
