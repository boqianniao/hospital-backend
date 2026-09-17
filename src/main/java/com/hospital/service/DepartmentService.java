package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hospital.entity.Department;
import com.hospital.entity.HospitalDepartment;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.HospitalDepartmentMapper;
import com.hospital.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 科室服务：一级/二级树形结构、按医院查询、名称映射。
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final HospitalDepartmentMapper hospitalDepartmentMapper;

    /** 全部科室树（一级 + children 二级） */
    public List<DepartmentVO> tree() {
        List<Department> all = departmentMapper.selectList(
                Wrappers.<Department>lambdaQuery()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSortOrder));
        return buildTree(all);
    }

    /** 某医院开设的科室树 */
    public List<DepartmentVO> treeByHospital(Long hospitalId) {
        List<HospitalDepartment> rels = hospitalDepartmentMapper.selectList(
                Wrappers.<HospitalDepartment>lambdaQuery().eq(HospitalDepartment::getHospitalId, hospitalId));
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> deptIds = rels.stream().map(HospitalDepartment::getDepartmentId).distinct().collect(Collectors.toList());
        // 关联的二级科室 + 其父级一级科室都要纳入，才能构成完整树
        List<Department> leaves = departmentMapper.selectBatchIds(deptIds);
        List<Long> parentIds = leaves.stream().map(Department::getParentId)
                .filter(pid -> pid != null && pid > 0).distinct().collect(Collectors.toList());
        Map<Long, Department> nodeMap = new HashMap<>();
        leaves.forEach(d -> nodeMap.put(d.getId(), d));
        if (!parentIds.isEmpty()) {
            departmentMapper.selectBatchIds(parentIds).forEach(d -> nodeMap.putIfAbsent(d.getId(), d));
        }
        return buildTree(new ArrayList<>(nodeMap.values()));
    }

    /** 科室 id -> name 映射，供其他模块拼接名称 */
    public Map<Long, String> nameMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        return departmentMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
    }

    public String name(Long id) {
        if (id == null) return null;
        Department d = departmentMapper.selectById(id);
        return d == null ? null : d.getName();
    }

    private List<DepartmentVO> buildTree(List<Department> all) {
        Map<Long, DepartmentVO> voMap = all.stream()
                .collect(Collectors.toMap(Department::getId, this::toVO, (a, b) -> a));
        List<DepartmentVO> roots = new ArrayList<>();
        for (Department d : all) {
            DepartmentVO vo = voMap.get(d.getId());
            Long parentId = d.getParentId() == null ? 0L : d.getParentId();
            if (parentId == 0L) {
                roots.add(vo);
            } else {
                DepartmentVO parent = voMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    // 父级未在集合中（如按医院过滤时），作为根返回
                    roots.add(vo);
                }
            }
        }
        roots.sort((a, b) -> nullSafe(a.getSortOrder()) - nullSafe(b.getSortOrder()));
        return roots;
    }

    private int nullSafe(Integer i) {
        return i == null ? 0 : i;
    }

    public DepartmentVO toVO(Department d) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtil.copyProperties(d, vo, "children");
        return vo;
    }

    /** 便于 Stream 使用的转换函数 */
    public Function<Department, DepartmentVO> converter() {
        return this::toVO;
    }
}
