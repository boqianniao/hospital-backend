package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.entity.Disease;
import com.hospital.mapper.DiseaseMapper;
import com.hospital.vo.DiseaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 疾病服务：分页/筛选、详情。
 */
@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseMapper diseaseMapper;
    private final DepartmentService departmentService;

    public PageResult<DiseaseVO> page(long pageNum, long pageSize, Long departmentId, String keyword) {
        LambdaQueryWrapper<Disease> qw = Wrappers.<Disease>lambdaQuery()
                .eq(departmentId != null, Disease::getDepartmentId, departmentId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Disease::getName, keyword).or()
                        .like(Disease::getAlias, keyword).or()
                        .like(Disease::getSymptoms, keyword))
                .orderByDesc(Disease::getFollowCount)
                .orderByAsc(Disease::getId);
        IPage<Disease> page = diseaseMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page, enrich(page.getRecords()));
    }

    public DiseaseVO detail(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return enrich(Collections.singletonList(disease)).get(0);
    }

    private List<DiseaseVO> enrich(List<Disease> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> deptIds = list.stream().map(Disease::getDepartmentId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        Map<Long, String> deptNames = departmentService.nameMap(deptIds);
        return list.stream().map(d -> {
            DiseaseVO vo = new DiseaseVO();
            BeanUtil.copyProperties(d, vo);
            vo.setDepartmentName(deptNames.get(d.getDepartmentId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
