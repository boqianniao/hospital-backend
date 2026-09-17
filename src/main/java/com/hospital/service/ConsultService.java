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
import com.hospital.dto.order.ConsultCreateDTO;
import com.hospital.entity.Consult;
import com.hospital.entity.Doctor;
import com.hospital.entity.FamilyMember;
import com.hospital.mapper.ConsultMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.FamilyMemberMapper;
import com.hospital.vo.ConsultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 电话咨询订单：下单、查询、取消(退款)、完成、超时取消。
 */
@Service
@RequiredArgsConstructor
public class ConsultService {

    private final ConsultMapper consultMapper;
    private final DoctorMapper doctorMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public ConsultVO create(Long userId, ConsultCreateDTO dto) {
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "医生不存在");
        }
        Consult order = new Consult();
        order.setOrderNo(OrderNoGenerator.consult());
        order.setUserId(userId);
        order.setDoctorId(doctor.getId());
        fillPatient(order, userId, dto);
        order.setDiseaseDesc(dto.getDiseaseDesc());
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setDuration(dto.getDuration() == null ? 30 : dto.getDuration());
        order.setAmount(doctor.getPrice());
        order.setStatus(OrderStatus.CONSULT_UNPAID);
        consultMapper.insert(order);

        notificationService.push(userId, "电话咨询下单成功",
                String.format("您已向 %s 提交电话咨询，请尽快完成支付。", doctor.getName()));
        return toVO(order, doctor.getName());
    }

    public PageResult<ConsultVO> myPage(Long userId, long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<Consult> qw = Wrappers.<Consult>lambdaQuery()
                .eq(Consult::getUserId, userId)
                .eq(status != null, Consult::getStatus, status)
                .orderByDesc(Consult::getId);
        IPage<Consult> page = consultMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page, enrich(page.getRecords()));
    }

    public ConsultVO detail(Long userId, Long id) {
        Consult order = getOwned(userId, id);
        return enrich(Collections.singletonList(order)).get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long id) {
        Consult order = getOwnedForUpdate(userId, id);
        Integer st = order.getStatus();
        if (st == null || st == OrderStatus.CONSULT_DONE || st == OrderStatus.CONSULT_CANCELLED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "该订单不可取消");
        }
        if (st == OrderStatus.CONSULT_PAID || st == OrderStatus.CONSULT_ING) {
            paymentService.refund(order.getOrderNo(), OrderStatus.BIZ_CONSULT, order.getAmount(), userId);
        }
        Consult upd = new Consult();
        upd.setId(order.getId());
        upd.setStatus(OrderStatus.CONSULT_CANCELLED);
        consultMapper.updateById(upd);
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Long userId, Long id) {
        Consult order = getOwnedForUpdate(userId, id);
        if (order.getStatus() == null
                || (order.getStatus() != OrderStatus.CONSULT_PAID && order.getStatus() != OrderStatus.CONSULT_ING)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "仅进行中/已支付订单可完成");
        }
        Consult upd = new Consult();
        upd.setId(order.getId());
        upd.setStatus(OrderStatus.CONSULT_DONE);
        consultMapper.updateById(upd);
        doctorMapper.incrConsultCount(order.getDoctorId());
    }

    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeout(LocalDateTime deadline) {
        List<Consult> list = consultMapper.selectList(Wrappers.<Consult>lambdaQuery()
                .eq(Consult::getStatus, OrderStatus.CONSULT_UNPAID)
                .lt(Consult::getCreateTime, deadline).last("for update"));
        for (Consult order : list) {
            Consult upd = new Consult();
            upd.setId(order.getId());
            upd.setStatus(OrderStatus.CONSULT_CANCELLED);
            consultMapper.updateById(upd);
            notificationService.push(order.getUserId(), "订单已取消",
                    String.format("咨询订单 %s 超时未支付，已自动取消。", order.getOrderNo()));
        }
        return list.size();
    }

    // ---- 内部方法 ----

    private Consult getOwnedForUpdate(Long userId, Long id) {
        Consult order = consultMapper.selectOne(Wrappers.<Consult>lambdaQuery()
                .eq(Consult::getId, id).last("for update"));
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);
        return order;
    }

    private Consult getOwned(Long userId, Long id) {
        Consult order = consultMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    private void fillPatient(Consult order, Long userId, ConsultCreateDTO dto) {
        if (dto.getFamilyMemberId() != null) {
            FamilyMember m = familyMemberMapper.selectById(dto.getFamilyMemberId());
            if (m == null || !m.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "就诊成员不存在或不属于当前用户");
            }
            order.setPatientName(m.getName());
            order.setPatientPhone(m.getPhone());
        } else {
            if (!StringUtils.hasText(dto.getPatientName())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请选择就诊成员或填写咨询人姓名");
            }
            order.setPatientName(dto.getPatientName());
            order.setPatientPhone(dto.getPatientPhone());
        }
    }

    private List<ConsultVO> enrich(List<Consult> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> docIds = list.stream().map(Consult::getDoctorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> docNames = docIds.isEmpty() ? Collections.emptyMap()
                : doctorMapper.selectBatchIds(docIds).stream().collect(Collectors.toMap(Doctor::getId, Doctor::getName, (a, b) -> a));
        return list.stream().map(o -> toVO(o, docNames.get(o.getDoctorId()))).collect(Collectors.toList());
    }

    private ConsultVO toVO(Consult o, String doctorName) {
        ConsultVO vo = new ConsultVO();
        BeanUtil.copyProperties(o, vo);
        vo.setDoctorName(doctorName);
        vo.setStatusText(OrderStatus.consultText(o.getStatus()));
        return vo;
    }
}
