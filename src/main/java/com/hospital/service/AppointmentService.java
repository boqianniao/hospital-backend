package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.constant.OrderStatus;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.common.util.OrderNoGenerator;
import com.hospital.config.props.HospitalProperties;
import com.hospital.dto.order.AppointmentCreateDTO;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import com.hospital.vo.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 挂号订单：下单(号源校验+Redis预扣+DB扣减)、查询、取消(退款+释放号源)、完成、超时取消。
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final DoctorService doctorService;
    private final HospitalProperties props;

    @Transactional(rollbackFor = Exception.class)
    public AppointmentVO create(Long userId, AppointmentCreateDTO dto) {
        Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }
        if (schedule.getScheduleDate() != null && schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "该号源日期已过期");
        }
        if ((schedule.getStatus() != null && schedule.getStatus() == 0)
                || (schedule.getRemainCount() != null && schedule.getRemainCount() <= 0)) {
            throw new BusinessException(ResultCode.NO_STOCK);
        }
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "医生不存在");
        }

        // 1. Redis 预扣
        if (!stockService.tryDeduct(schedule.getId(), safe(schedule.getRemainCount()))) {
            throw new BusinessException(ResultCode.NO_STOCK);
        }
        // 2. DB 原子扣减（最终事实），失败回补 Redis
        if (scheduleMapper.deductStock(schedule.getId()) == 0) {
            stockService.restore(schedule.getId());
            throw new BusinessException(ResultCode.NO_STOCK);
        }

        // 3. 组装订单
        Appointment order = new Appointment();
        order.setOrderNo(OrderNoGenerator.appointment());
        order.setUserId(userId);
        order.setDoctorId(schedule.getDoctorId());
        order.setHospitalId(schedule.getHospitalId());
        fillPatient(order, userId, dto);
        order.setAppointmentDate(schedule.getScheduleDate());
        order.setAppointmentTime(schedule.getTimeSlot());
        order.setDiseaseDesc(dto.getDiseaseDesc());
        order.setAmount(doctor.getRegistrationPrice());
        order.setStatus(OrderStatus.APPT_UNPAID);
        appointmentMapper.insert(order);

        notificationService.push(userId, "挂号下单成功",
                String.format("您已成功预约 %s %s 的号源，请在%d分钟内完成支付。",
                        doctor.getName(), schedule.getTimeSlot(), props.getOrder().getTimeoutMinutes()));
        Hospital hospital = order.getHospitalId() == null ? null : hospitalMapper.selectById(order.getHospitalId());
        Department department = doctor.getDepartmentId() == null ? null : departmentMapper.selectById(doctor.getDepartmentId());
        return toVO(order, doctor, hospital, department == null ? null : department.getName());
    }

    /** 我的挂号（可按状态过滤） */
    public PageResult<AppointmentVO> myPage(Long userId, long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<Appointment> qw = Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getUserId, userId)
                .eq(status != null, Appointment::getStatus, status)
                .orderByDesc(Appointment::getId);
        IPage<Appointment> page = appointmentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page, enrich(page.getRecords()));
    }

    public AppointmentVO detail(Long userId, Long id) {
        Appointment order = getOwned(userId, id);
        return enrich(Collections.singletonList(order)).get(0);
    }

    /** 取消挂号：待支付直接取消并释放号源；已支付退款并释放号源 */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long id) {
        Appointment order = getOwnedForUpdate(userId, id);
        Integer st = order.getStatus();
        if (st == null || st == OrderStatus.APPT_DONE || st == OrderStatus.APPT_CANCELLED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "该订单不可取消");
        }
        if (st == OrderStatus.APPT_PAID) {
            paymentService.refund(order.getOrderNo(), OrderStatus.BIZ_APPOINTMENT, order.getAmount(), userId);
        }
        Appointment upd = new Appointment();
        upd.setId(order.getId());
        upd.setStatus(OrderStatus.APPT_CANCELLED);
        appointmentMapper.updateById(upd);
        releaseSchedule(order);
    }

    /** 完成挂号：已支付 -> 已完成，医生接诊数+1 */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long userId, Long id) {
        Appointment order = getOwnedForUpdate(userId, id);
        if (order.getStatus() == null || order.getStatus() != OrderStatus.APPT_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "仅已支付订单可完成");
        }
        Appointment upd = new Appointment();
        upd.setId(order.getId());
        upd.setStatus(OrderStatus.APPT_DONE);
        appointmentMapper.updateById(upd);
        doctorMapper.incrConsultCount(order.getDoctorId());
        doctorService.evictCache(order.getDoctorId());
    }

    /** 超时未支付自动取消，返回取消数量（供定时任务调用） */
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeout(LocalDateTime deadline) {
        List<Appointment> list = appointmentMapper.selectList(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getStatus, OrderStatus.APPT_UNPAID)
                .lt(Appointment::getCreateTime, deadline).last("for update"));
        for (Appointment order : list) {
            Appointment upd = new Appointment();
            upd.setId(order.getId());
            upd.setStatus(OrderStatus.APPT_CANCELLED);
            appointmentMapper.updateById(upd);
            releaseSchedule(order);
            notificationService.push(order.getUserId(), "订单已取消",
                    String.format("挂号订单 %s 超时未支付，已自动取消，号源已释放。", order.getOrderNo()));
        }
        return list.size();
    }

    // ---- 内部方法 ----

    private Appointment getOwnedForUpdate(Long userId, Long id) {
        Appointment order = appointmentMapper.selectOne(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getId, id).last("for update"));
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);
        return order;
    }

    private Appointment getOwned(Long userId, Long id) {
        Appointment order = appointmentMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    /** 依据 医生+医院+日期+时段 定位号源并回补（DB+Redis） */
    private void releaseSchedule(Appointment order) {
        Schedule schedule = scheduleMapper.selectOne(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getDoctorId, order.getDoctorId())
                .eq(Schedule::getHospitalId, order.getHospitalId())
                .eq(Schedule::getScheduleDate, order.getAppointmentDate())
                .eq(Schedule::getTimeSlot, order.getAppointmentTime())
                .last("limit 1"));
        if (schedule != null) {
            scheduleMapper.restoreStock(schedule.getId());
            stockService.restore(schedule.getId());
        }
    }

    private void fillPatient(Appointment order, Long userId, AppointmentCreateDTO dto) {
        if (dto.getFamilyMemberId() != null) {
            FamilyMember m = familyMemberMapper.selectById(dto.getFamilyMemberId());
            if (m == null || !m.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "就诊成员不存在或不属于当前用户");
            }
            order.setPatientName(m.getName());
            order.setPatientPhone(m.getPhone());
            order.setPatientIdCard(m.getIdCard());
            order.setPatientGender(m.getGender());
            if (m.getBirthday() != null) {
                order.setPatientAge(Period.between(m.getBirthday(), LocalDate.now()).getYears());
            }
        } else {
            if (!StringUtils.hasText(dto.getPatientName())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请选择就诊成员或填写就诊人姓名");
            }
            order.setPatientName(dto.getPatientName());
            order.setPatientPhone(dto.getPatientPhone());
            order.setPatientIdCard(dto.getPatientIdCard());
            order.setPatientGender(dto.getPatientGender());
            order.setPatientAge(dto.getPatientAge());
        }
    }

    private List<AppointmentVO> enrich(List<Appointment> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> docIds = list.stream().map(Appointment::getDoctorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> hospIds = list.stream().map(Appointment::getHospitalId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Doctor> doctors = docIds.isEmpty() ? Collections.emptyMap()
                : doctorMapper.selectBatchIds(docIds).stream().collect(Collectors.toMap(Doctor::getId, d -> d, (a, b) -> a));
        Map<Long, Hospital> hospitals = hospIds.isEmpty() ? Collections.emptyMap()
                : hospitalMapper.selectBatchIds(hospIds).stream().collect(Collectors.toMap(Hospital::getId, h -> h, (a, b) -> a));
        List<Long> deptIds = doctors.values().stream().map(Doctor::getDepartmentId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> departmentNames = deptIds.isEmpty() ? Collections.emptyMap()
                : departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        return list.stream()
                .map(o -> {
                    Doctor doctor = doctors.get(o.getDoctorId());
                    return toVO(o, doctor, hospitals.get(o.getHospitalId()),
                            doctor == null ? null : departmentNames.get(doctor.getDepartmentId()));
                })
                .collect(Collectors.toList());
    }

    private AppointmentVO toVO(Appointment o, Doctor doctor, Hospital hospital, String departmentName) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtil.copyProperties(o, vo);
        if (doctor != null) {
            vo.setDoctorName(doctor.getName());
            vo.setDoctorTitle(doctor.getTitle());
            vo.setDoctorAvatar(doctor.getAvatar());
        }
        vo.setDepartmentName(departmentName);
        if (hospital != null) {
            vo.setHospitalName(hospital.getName());
            vo.setHospitalImage(hospital.getImage());
        }
        vo.setStatusText(OrderStatus.appointmentText(o.getStatus()));
        return vo;
    }

    private int safe(Integer i) {
        return i == null ? 0 : i;
    }
}
