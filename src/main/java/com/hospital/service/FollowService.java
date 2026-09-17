package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.constant.FollowType;
import com.hospital.common.constant.RedisKeys;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import com.hospital.vo.FollowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注：医院/医生/疾病的关注与取消，同步维护关注计数，失效相关缓存。
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;
    private final DiseaseMapper diseaseMapper;
    private final CacheService cacheService;

    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Integer type, Long targetId) {
        if (!FollowType.valid(type)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "关注类型错误");
        }
        assertTargetExists(type, targetId);
        // 幂等：已关注直接返回
        Long exists = followMapper.selectCount(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        if (exists != null && exists > 0) {
            return;
        }
        Follow f = new Follow();
        f.setUserId(userId);
        f.setFollowType(type);
        f.setFollowId(targetId);
        followMapper.insert(f);
        changeCount(type, targetId, 1);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Integer type, Long targetId) {
        int rows = followMapper.delete(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        if (rows > 0) {
            changeCount(type, targetId, -1);
        }
    }

    public boolean isFollowing(Long userId, Integer type, Long targetId) {
        Long c = followMapper.selectCount(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        return c != null && c > 0;
    }

    public PageResult<FollowVO> myFollows(Long userId, Integer type, long pageNum, long pageSize) {
        LambdaQueryWrapper<Follow> qw = Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(type != null, Follow::getFollowType, type)
                .orderByDesc(Follow::getId);
        IPage<Follow> page = followMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<FollowVO> vos = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page, vos);
    }

    private void changeCount(Integer type, Long targetId, int delta) {
        switch (type) {
            case FollowType.HOSPITAL -> {
                hospitalMapper.updateFollowCount(targetId, delta);
                cacheService.evict(RedisKeys.hospital(targetId));
            }
            case FollowType.DOCTOR -> {
                doctorMapper.updateFollowCount(targetId, delta);
                cacheService.evict(RedisKeys.doctor(targetId));
            }
            case FollowType.DISEASE -> diseaseMapper.updateFollowCount(targetId, delta);
            default -> {
            }
        }
    }

    private void assertTargetExists(Integer type, Long targetId) {
        boolean exists = switch (type) {
            case FollowType.HOSPITAL -> hospitalMapper.selectById(targetId) != null;
            case FollowType.DOCTOR -> doctorMapper.selectById(targetId) != null;
            case FollowType.DISEASE -> diseaseMapper.selectById(targetId) != null;
            default -> false;
        };
        if (!exists) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "关注对象不存在");
        }
    }

    private FollowVO toVO(Follow f) {
        FollowVO vo = new FollowVO();
        vo.setId(f.getId());
        vo.setFollowType(f.getFollowType());
        vo.setFollowId(f.getFollowId());
        vo.setCreateTime(f.getCreateTime());
        switch (f.getFollowType()) {
            case FollowType.HOSPITAL -> {
                Hospital h = hospitalMapper.selectById(f.getFollowId());
                if (h != null) {
                    vo.setTargetName(h.getName());
                    vo.setTargetImage(h.getImage());
                    vo.setTargetIntro(h.getIntro());
                }
            }
            case FollowType.DOCTOR -> {
                Doctor d = doctorMapper.selectById(f.getFollowId());
                if (d != null) {
                    vo.setTargetName(d.getName());
                    vo.setTargetImage(d.getAvatar());
                    vo.setTargetIntro(d.getIntro());
                }
            }
            case FollowType.DISEASE -> {
                Disease ds = diseaseMapper.selectById(f.getFollowId());
                if (ds != null) {
                    vo.setTargetName(ds.getName());
                    vo.setTargetIntro(ds.getDescription());
                }
            }
            default -> {
            }
        }
        return vo;
    }
}
