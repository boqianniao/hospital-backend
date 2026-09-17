package com.hospital.task;

import com.hospital.config.props.HospitalProperties;
import com.hospital.service.AppointmentService;
import com.hospital.service.ConsultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单超时自动取消：周期性扫描超过支付时限仍未支付的挂号/咨询订单，
 * 取消订单并释放号源。取消与号源回补逻辑复用各订单服务已有的 cancelTimeout。
 * <p>时限取 hospital.order.timeout-minutes；扫描周期取 hospital.order.timeout-scan-ms（默认 60s）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final AppointmentService appointmentService;
    private final ConsultService consultService;
    private final HospitalProperties props;

    /** 应用启动后延迟一个扫描周期首次执行，之后按固定间隔轮询。 */
    @Scheduled(
            fixedDelayString = "${hospital.order.timeout-scan-ms:60000}",
            initialDelayString = "${hospital.order.timeout-scan-ms:60000}")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(props.getOrder().getTimeoutMinutes());
        try {
            int appointments = appointmentService.cancelTimeout(deadline);
            int consults = consultService.cancelTimeout(deadline);
            if (appointments > 0 || consults > 0) {
                log.info("[ORDER-TIMEOUT] 自动取消超时订单 挂号={} 咨询={} deadline={}", appointments, consults, deadline);
            } else {
                log.debug("[ORDER-TIMEOUT] 扫描完成，无超时订单 deadline={}", deadline);
            }
        } catch (Exception e) {
            log.error("[ORDER-TIMEOUT] 超时取消任务执行异常: {}", e.getMessage(), e);
        }
    }
}
