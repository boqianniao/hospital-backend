package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hospital.common.exception.BusinessException;
import com.hospital.dto.FamilyMemberDTO;
import com.hospital.entity.FamilyMember;
import com.hospital.mapper.FamilyMemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 就诊成员：首个自动默认、设默认互斥、删除默认后自动补位、越权保护。
 */
class FamilyMemberServiceTest {
    FamilyMemberMapper mapper = mock(FamilyMemberMapper.class);
    FamilyMemberService service = new FamilyMemberService(mapper);

    FamilyMemberDTO dto() {
        FamilyMemberDTO dto = new FamilyMemberDTO();
        dto.setName("李四");
        dto.setPhone("13900000000");
        dto.setGender(1);
        return dto;
    }

    @BeforeEach
    void resetMock() {
        reset(mapper);
    }

    @Test
    void firstMemberBecomesDefault() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        FamilyMember saved = service.add(7L, dto());
        assertEquals(1, saved.getIsDefault());
        assertEquals(7L, saved.getUserId());
        verify(mapper).insert(argThat((FamilyMember m) -> m.getIsDefault() == 1));
    }

    @Test
    void nonDefaultMemberWhenOthersExist() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(2L);
        FamilyMemberDTO dto = dto();
        dto.setIsDefault(0);
        FamilyMember saved = service.add(7L, dto);
        assertEquals(0, saved.getIsDefault());
        // 未设默认时不应清除其它默认成员
        verify(mapper, never()).update(any(FamilyMember.class), any(Wrapper.class));
    }

    @Test
    void explicitDefaultClearsOthers() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(2L);
        FamilyMemberDTO dto = dto();
        dto.setIsDefault(1);
        service.add(7L, dto);
        // 清除旧默认（update ... set is_default=0）
        verify(mapper).update(any(FamilyMember.class), any(Wrapper.class));
        verify(mapper).insert(argThat((FamilyMember m) -> m.getIsDefault() == 1));
    }

    @Test
    void setDefaultClearsOthersThenMarksTarget() {
        FamilyMember m = new FamilyMember();
        m.setId(5L);
        m.setUserId(7L);
        when(mapper.selectById(5L)).thenReturn(m);
        service.setDefault(7L, 5L);
        var order = inOrder(mapper);
        order.verify(mapper).update(any(FamilyMember.class), any(Wrapper.class)); // clearDefault
        order.verify(mapper).updateById(argThat((FamilyMember x) -> x.getIsDefault() == 1));
    }

    @Test
    void deletingDefaultReassignsToRemaining() {
        FamilyMember target = new FamilyMember();
        target.setId(5L);
        target.setUserId(7L);
        target.setIsDefault(1);
        when(mapper.selectById(5L)).thenReturn(target);
        FamilyMember remaining = new FamilyMember();
        remaining.setId(6L);
        remaining.setUserId(7L);
        remaining.setIsDefault(0);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(remaining));

        service.delete(7L, 5L);

        verify(mapper).deleteById(5L);
        verify(mapper).updateById(argThat((FamilyMember x) -> x.getId().equals(6L) && x.getIsDefault() == 1));
    }

    @Test
    void cannotOperateOthersMember() {
        FamilyMember m = new FamilyMember();
        m.setId(5L);
        m.setUserId(8L);
        when(mapper.selectById(5L)).thenReturn(m);
        assertThrows(BusinessException.class, () -> service.setDefault(7L, 5L));
        verify(mapper, never()).updateById(any(FamilyMember.class));
    }
}
