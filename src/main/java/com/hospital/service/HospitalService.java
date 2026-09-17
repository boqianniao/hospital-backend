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
import com.hospital.entity.Hospital;
import com.hospital.mapper.HospitalMapper;
import com.hospital.vo.HospitalDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 医院服务：分页/筛选、详情（Redis 缓存）。
 */
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalMapper hospitalMapper;
    private final DepartmentService departmentService;
    private final CacheService cacheService;

    /** 分页查询（关键词/省/市/等级过滤，仅正常状态） */
    public PageResult<Hospital> page(long pageNum, long pageSize, String keyword,
                                     String province, String city, String level) {
        LambdaQueryWrapper<Hospital> qw = Wrappers.<Hospital>lambdaQuery()
                .eq(Hospital::getStatus, 1)
                .like(StringUtils.hasText(keyword), Hospital::getName, keyword)
                .eq(StringUtils.hasText(province), Hospital::getProvince, province)
                .eq(StringUtils.hasText(city), Hospital::getCity, city)
                .eq(StringUtils.hasText(level), Hospital::getLevel, level)
                .orderByDesc(Hospital::getFollowCount)
                .orderByAsc(Hospital::getId);
        IPage<Hospital> page = hospitalMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page);
    }

    /** 详情（含科室树），Redis 缓存 */
    public HospitalDetailVO detail(Long id) {
        return cacheService.getOrLoad(RedisKeys.hospital(id), HospitalDetailVO.class, () -> loadDetail(id));
    }

    private HospitalDetailVO loadDetail(Long id) {
        Hospital hospital = hospitalMapper.selectById(id);
        if (hospital == null || (hospital.getStatus() != null && hospital.getStatus() == 0)) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        HospitalDetailVO vo = new HospitalDetailVO();
        BeanUtil.copyProperties(hospital, vo, "departments");
        vo.setDepartments(departmentService.treeByHospital(id));
        return vo;
    }

    public Hospital getById(Long id) {
        return hospitalMapper.selectById(id);
    }

    /** 详情缓存失效 */
    public void evictCache(Long id) {
        cacheService.evict(RedisKeys.hospital(id));
    }
}
