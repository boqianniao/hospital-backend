package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hospital.common.constant.FollowType;
import com.hospital.common.exception.BusinessException;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.mapper.DiseaseMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.FollowMapper;
import com.hospital.mapper.HospitalMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hospital.entity.Follow;

/**
 * 关注：幂等（已关注不重复插入）、计数联动、非法类型、目标不存在、取消关注。
 */
class FollowServiceTest {
    FollowMapper followMapper = mock(FollowMapper.class);
    HospitalMapper hospitalMapper = mock(HospitalMapper.class);
    DoctorMapper doctorMapper = mock(DoctorMapper.class);
    DiseaseMapper diseaseMapper = mock(DiseaseMapper.class);
    CacheService cacheService = mock(CacheService.class);
    FollowService service = new FollowService(followMapper, hospitalMapper, doctorMapper, diseaseMapper, cacheService);

    @Test
    void followHospitalInsertsAndIncrementsCount() {
        when(hospitalMapper.selectById(2L)).thenReturn(new Hospital());
        when(followMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        service.follow(7L, FollowType.HOSPITAL, 2L);
        verify(followMapper).insert(any(Follow.class));
        verify(hospitalMapper).updateFollowCount(2L, 1);
        verify(cacheService).evict(anyString());
    }

    @Test
    void followIsIdempotentWhenAlreadyFollowing() {
        when(doctorMapper.selectById(3L)).thenReturn(new Doctor());
        when(followMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        service.follow(7L, FollowType.DOCTOR, 3L);
        verify(followMapper, never()).insert(any(Follow.class));
        verify(doctorMapper, never()).updateFollowCount(anyLong(), anyInt());
    }

    @Test
    void followInvalidTypeRejected() {
        assertThrows(BusinessException.class, () -> service.follow(7L, 99, 1L));
        verify(followMapper, never()).insert(any(Follow.class));
    }

    @Test
    void followNonexistentTargetRejected() {
        when(hospitalMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.follow(7L, FollowType.HOSPITAL, 999L));
        verify(followMapper, never()).insert(any(Follow.class));
    }

    @Test
    void unfollowDecrementsCountWhenRemoved() {
        when(followMapper.delete(any(Wrapper.class))).thenReturn(1);
        service.unfollow(7L, FollowType.HOSPITAL, 2L);
        verify(hospitalMapper).updateFollowCount(2L, -1);
    }

    @Test
    void unfollowNoOpWhenNothingRemoved() {
        when(followMapper.delete(any(Wrapper.class))).thenReturn(0);
        service.unfollow(7L, FollowType.HOSPITAL, 2L);
        verify(hospitalMapper, never()).updateFollowCount(anyLong(), anyInt());
    }
}
