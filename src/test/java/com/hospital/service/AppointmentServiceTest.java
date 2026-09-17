package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hospital.common.constant.OrderStatus;
import com.hospital.common.exception.BusinessException;
import com.hospital.config.props.HospitalProperties;
import com.hospital.dto.order.AppointmentCreateDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.FamilyMemberMapper;
import com.hospital.mapper.HospitalMapper;
import com.hospital.mapper.ScheduleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 挂号下单核心链路单测：号源校验、Redis 预扣 + DB 原子扣减、失败回补、取消释放、越权。
 */
class AppointmentServiceTest {
    AppointmentMapper appointmentMapper = mock(AppointmentMapper.class);
    ScheduleMapper scheduleMapper = mock(ScheduleMapper.class);
    DoctorMapper doctorMapper = mock(DoctorMapper.class);
    HospitalMapper hospitalMapper = mock(HospitalMapper.class);
    FamilyMemberMapper familyMemberMapper = mock(FamilyMemberMapper.class);
    StockService stockService = mock(StockService.class);
    PaymentService paymentService = mock(PaymentService.class);
    NotificationService notificationService = mock(NotificationService.class);
    DoctorService doctorService = mock(DoctorService.class);
    HospitalProperties props = new HospitalProperties();
    AppointmentService service;

    Schedule schedule;
    Doctor doctor;

    @BeforeEach
    void setup() {
        service = new AppointmentService(appointmentMapper, scheduleMapper, doctorMapper, hospitalMapper,
                familyMemberMapper, stockService, paymentService, notificationService, doctorService, props);
        schedule = new Schedule();
        schedule.setId(10L);
        schedule.setScheduleDate(LocalDate.now().plusDays(1));
        schedule.setStatus(1);
        schedule.setRemainCount(5);
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setTimeSlot("上午 09:00");
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("王医生");
        doctor.setRegistrationPrice(new BigDecimal("50.00"));
        Hospital hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("协和医院");
        when(scheduleMapper.selectById(10L)).thenReturn(schedule);
        when(doctorMapper.selectById(1L)).thenReturn(doctor);
        when(hospitalMapper.selectById(1L)).thenReturn(hospital);
    }

    AppointmentCreateDTO dto() {
        AppointmentCreateDTO dto = new AppointmentCreateDTO();
        dto.setScheduleId(10L);
        dto.setPatientName("张三");
        dto.setPatientPhone("13800000000");
        dto.setDiseaseDesc("头痛");
        return dto;
    }

    @Test
    void createSucceedsWithRedisAndDbDeduction() {
        when(stockService.tryDeduct(10L, 5)).thenReturn(true);
        when(scheduleMapper.deductStock(10L)).thenReturn(1);

        var vo = service.create(7L, dto());

        assertEquals(OrderStatus.APPT_UNPAID, vo.getStatus());
        assertEquals(0, new BigDecimal("50.00").compareTo(vo.getAmount()));
        assertEquals("王医生", vo.getDoctorName());
        assertEquals("协和医院", vo.getHospitalName());
        assertTrue(vo.getOrderNo().startsWith("GH"));
        // 先 Redis 预扣，再 DB 扣减，最后落单并通知
        var order = inOrder(stockService, scheduleMapper, appointmentMapper);
        order.verify(stockService).tryDeduct(10L, 5);
        order.verify(scheduleMapper).deductStock(10L);
        order.verify(appointmentMapper).insert(any(Appointment.class));
        verify(notificationService).push(eq(7L), anyString(), anyString());
    }

    @Test
    void createRejectedWhenRedisStockInsufficient() {
        when(stockService.tryDeduct(10L, 5)).thenReturn(false);
        assertThrows(BusinessException.class, () -> service.create(7L, dto()));
        verify(scheduleMapper, never()).deductStock(anyLong());
        verify(appointmentMapper, never()).insert(any(Appointment.class));
    }

    @Test
    void createRestoresRedisWhenDbDeductionFails() {
        when(stockService.tryDeduct(10L, 5)).thenReturn(true);
        when(scheduleMapper.deductStock(10L)).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.create(7L, dto()));
        verify(stockService).restore(10L);
        verify(appointmentMapper, never()).insert(any(Appointment.class));
    }

    @Test
    void createRejectedForExpiredSchedule() {
        schedule.setScheduleDate(LocalDate.now().minusDays(1));
        assertThrows(BusinessException.class, () -> service.create(7L, dto()));
        verify(stockService, never()).tryDeduct(anyLong(), anyInt());
    }

    @Test
    void createRejectedWhenNoRemainCount() {
        schedule.setRemainCount(0);
        assertThrows(BusinessException.class, () -> service.create(7L, dto()));
        verify(stockService, never()).tryDeduct(anyLong(), anyInt());
    }

    @Test
    void cancelUnpaidReleasesStockWithoutRefund() {
        Appointment order = new Appointment();
        order.setId(1L);
        order.setUserId(7L);
        order.setOrderNo("GH1");
        order.setStatus(OrderStatus.APPT_UNPAID);
        order.setDoctorId(1L);
        order.setHospitalId(1L);
        order.setAppointmentDate(schedule.getScheduleDate());
        order.setAppointmentTime("上午 09:00");
        when(appointmentMapper.selectOne(any(Wrapper.class))).thenReturn(order);
        when(scheduleMapper.selectOne(any(Wrapper.class))).thenReturn(schedule);

        service.cancel(7L, 1L);

        verify(appointmentMapper).updateById(argThat((Appointment a) -> a.getStatus() == OrderStatus.APPT_CANCELLED));
        verify(scheduleMapper).restoreStock(10L);
        verify(stockService).restore(10L);
        verifyNoInteractions(paymentService);
    }

    @Test
    void cancelPaidTriggersRefund() {
        Appointment order = new Appointment();
        order.setId(1L);
        order.setUserId(7L);
        order.setOrderNo("GH1");
        order.setStatus(OrderStatus.APPT_PAID);
        order.setAmount(new BigDecimal("50.00"));
        order.setDoctorId(1L);
        order.setHospitalId(1L);
        order.setAppointmentDate(schedule.getScheduleDate());
        order.setAppointmentTime("上午 09:00");
        when(appointmentMapper.selectOne(any(Wrapper.class))).thenReturn(order);
        when(scheduleMapper.selectOne(any(Wrapper.class))).thenReturn(schedule);

        service.cancel(7L, 1L);

        verify(paymentService).refund(eq("GH1"), eq(OrderStatus.BIZ_APPOINTMENT), any(BigDecimal.class), eq(7L));
        verify(appointmentMapper).updateById(argThat((Appointment a) -> a.getStatus() == OrderStatus.APPT_CANCELLED));
    }

    @Test
    void cancelRejectsOtherUsersOrder() {
        Appointment order = new Appointment();
        order.setId(1L);
        order.setUserId(8L);
        order.setStatus(OrderStatus.APPT_UNPAID);
        when(appointmentMapper.selectOne(any(Wrapper.class))).thenReturn(order);
        assertThrows(BusinessException.class, () -> service.cancel(7L, 1L));
        verify(appointmentMapper, never()).updateById(any(Appointment.class));
    }
}
