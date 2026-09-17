package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.dto.FamilyMemberDTO;
import com.hospital.entity.FamilyMember;
import com.hospital.mapper.FamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 就诊成员管理：增删改查 + 设默认（同一用户仅一个默认）。
 */
@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberMapper mapper;

    public List<FamilyMember> list(Long userId) {
        return mapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getUserId, userId)
                .orderByDesc(FamilyMember::getIsDefault)
                .orderByDesc(FamilyMember::getId));
    }

    public FamilyMember getOwned(Long userId, Long id) {
        FamilyMember m = mapper.selectById(id);
        if (m == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        if (!m.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public FamilyMember add(Long userId, FamilyMemberDTO dto) {
        FamilyMember m = new FamilyMember();
        BeanUtil.copyProperties(dto, m);
        m.setUserId(userId);
        // 首个成员自动设为默认
        long count = mapper.selectCount(Wrappers.<FamilyMember>lambdaQuery().eq(FamilyMember::getUserId, userId));
        boolean setDefault = (dto.getIsDefault() != null && dto.getIsDefault() == 1) || count == 0;
        m.setIsDefault(setDefault ? 1 : 0);
        if (setDefault) {
            clearDefault(userId);
        }
        mapper.insert(m);
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public FamilyMember update(Long userId, Long id, FamilyMemberDTO dto) {
        FamilyMember m = getOwned(userId, id);
        BeanUtil.copyProperties(dto, m, "id", "userId", "isDefault");
        boolean setDefault = dto.getIsDefault() != null && dto.getIsDefault() == 1;
        if (setDefault) {
            clearDefault(userId);
            m.setIsDefault(1);
        }
        mapper.updateById(m);
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        FamilyMember m = getOwned(userId, id);
        mapper.deleteById(id);
        // 若删除的是默认成员，则把最近的一个设为默认
        if (m.getIsDefault() != null && m.getIsDefault() == 1) {
            List<FamilyMember> rest = list(userId);
            if (!rest.isEmpty()) {
                FamilyMember first = rest.get(0);
                first.setIsDefault(1);
                mapper.updateById(first);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        FamilyMember m = getOwned(userId, id);
        clearDefault(userId);
        m.setIsDefault(1);
        mapper.updateById(m);
    }

    private void clearDefault(Long userId) {
        FamilyMember reset = new FamilyMember();
        reset.setIsDefault(0);
        mapper.update(reset, Wrappers.<FamilyMember>lambdaUpdate()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getIsDefault, 1));
    }
}
