package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.constant.RedisKeys;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.entity.Schedule;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.HospitalMapper;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.vo.DoctorVO;
import com.hospital.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医生服务：分页/筛选、详情（缓存）、排班查询。
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorMapper doctorMapper;
    private final HospitalMapper hospitalMapper;
    private final ScheduleMapper scheduleMapper;
    private final DepartmentService departmentService;
    private final CacheService cacheService;

    public PageResult<DoctorVO> page(long pageNum, long pageSize, Long hospitalId,
                                     Long departmentId, String keyword) {
        LambdaQueryWrapper<Doctor> qw = Wrappers.<Doctor>lambdaQuery()
                .eq(Doctor::getStatus, 1)
                .eq(hospitalId != null, Doctor::getHospitalId, hospitalId)
                .eq(departmentId != null, Doctor::getDepartmentId, departmentId)
                .like(StringUtils.hasText(keyword), Doctor::getName, keyword)
                .orderByDesc(Doctor::getRating)
                .orderByDesc(Doctor::getConsultCount);
        IPage<Doctor> page = doctorMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<DoctorVO> vos = enrich(page.getRecords());
        return PageResult.of(page, vos);
    }

    /** 详情（缓存） */
    public DoctorVO detail(Long id) {
        return cacheService.getOrLoad(RedisKeys.doctor(id), DoctorVO.class, () -> {
            Doctor doctor = doctorMapper.selectById(id);
            if (doctor == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
            }
            return enrich(Collections.singletonList(doctor)).get(0);
        });
    }

    /** 医生排班（可选日期范围，默认今天起） */
    public List<ScheduleVO> schedules(Long doctorId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now();
        LambdaQueryWrapper<Schedule> qw = Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getDoctorId, doctorId)
                .ge(Schedule::getScheduleDate, start)
                .le(to != null, Schedule::getScheduleDate, to)
                .orderByAsc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getTimeSlot);
        return scheduleMapper.selectList(qw).stream().map(s -> {
            ScheduleVO vo = new ScheduleVO();
            BeanUtil.copyProperties(s, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    public void evictCache(Long id) {
        cacheService.evict(RedisKeys.doctor(id));
    }

    /** 批量补充科室/医院名称，避免 N+1 */
    private List<DoctorVO> enrich(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> deptIds = doctors.stream().map(Doctor::getDepartmentId).filter(java.util.Objects::nonNull)
                .distinct().collect(Collectors.toList());
        List<Long> hospIds = doctors.stream().map(Doctor::getHospitalId).filter(java.util.Objects::nonNull)
                .distinct().collect(Collectors.toList());
        Map<Long, String> deptNames = departmentService.nameMap(deptIds);
        Map<Long, String> hospNames = hospIds.isEmpty() ? Collections.emptyMap()
                : hospitalMapper.selectBatchIds(hospIds).stream()
                .collect(Collectors.toMap(Hospital::getId, Hospital::getName, (a, b) -> a));
        return doctors.stream().map(d -> {
            DoctorVO vo = new DoctorVO();
            BeanUtil.copyProperties(d, vo);
            vo.setDepartmentName(deptNames.get(d.getDepartmentId()));
            vo.setHospitalName(hospNames.get(d.getHospitalId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
