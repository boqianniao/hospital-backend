package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.constant.OrderStatus;
import com.hospital.common.constant.RedisKeys;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.dto.ReviewDTO;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import com.hospital.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 评价：提交（校验订单归属，防重复，重算医生评分）、按医生查询、我的评价。
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    @Transactional(rollbackFor = Exception.class)
    public ReviewVO submit(Long userId, ReviewDTO dto) {
        Long doctorId = resolveDoctorId(userId, dto.getOrderType(), dto.getOrderId());
        // 防重复评价（同一订单只可评价一次）
        Long dup = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderType, dto.getOrderType())
                .eq(Review::getOrderId, dto.getOrderId()));
        if (dup != null && dup > 0) {
            throw new BusinessException("该订单已评价，请勿重复提交");
        }
        Review review = new Review();
        review.setOrderType(dto.getOrderType());
        review.setOrderId(dto.getOrderId());
        review.setUserId(userId);
        review.setDoctorId(doctorId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        reviewMapper.insert(review);

        // 重算医生平均分
        Double avg = reviewMapper.avgRatingByDoctor(doctorId);
        if (avg != null) {
            Doctor upd = new Doctor();
            upd.setId(doctorId);
            upd.setRating(BigDecimal.valueOf(avg));
            doctorMapper.updateById(upd);
            // 评分变更后失效医生详情缓存，避免详情页展示旧评分
            cacheService.evict(RedisKeys.doctor(doctorId));
        }
        return enrich(Collections.singletonList(review)).get(0);
    }

    /** 某医生的评价列表（公开） */
    public PageResult<ReviewVO> listByDoctor(Long doctorId, long pageNum, long pageSize) {
        IPage<Review> page = reviewMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Review>lambdaQuery().eq(Review::getDoctorId, doctorId).orderByDesc(Review::getId));
        return PageResult.of(page, enrich(page.getRecords()));
    }

    /** 我的评价 */
    public PageResult<ReviewVO> myReviews(Long userId, long pageNum, long pageSize) {
        IPage<Review> page = reviewMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Review>lambdaQuery().eq(Review::getUserId, userId).orderByDesc(Review::getId));
        return PageResult.of(page, enrich(page.getRecords()));
    }

    /** 校验订单归属并返回医生ID；要求订单已支付/完成 */
    private Long resolveDoctorId(Long userId, Integer orderType, Long orderId) {
        if (orderType != null && orderType == OrderStatus.BIZ_APPOINTMENT) {
            Appointment a = appointmentMapper.selectById(orderId);
            if (a == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            if (!a.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);
            if (a.getStatus() == null || a.getStatus() < OrderStatus.APPT_PAID
                    || a.getStatus() == OrderStatus.APPT_CANCELLED) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单未完成，暂不能评价");
            }
            return a.getDoctorId();
        } else if (orderType != null && orderType == OrderStatus.BIZ_CONSULT) {
            Consult c = consultMapper.selectById(orderId);
            if (c == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            if (!c.getUserId().equals(userId)) throw new BusinessException(ResultCode.FORBIDDEN);
            if (c.getStatus() == null || c.getStatus() < OrderStatus.CONSULT_PAID
                    || c.getStatus() == OrderStatus.CONSULT_CANCELLED) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单未完成，暂不能评价");
            }
            return c.getDoctorId();
        }
        throw new BusinessException(ResultCode.PARAM_ERROR, "订单类型错误");
    }

    private List<ReviewVO> enrich(List<Review> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> docIds = list.stream().map(Review::getDoctorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> userIds = list.stream().map(Review::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> docNames = docIds.isEmpty() ? Collections.emptyMap()
                : doctorMapper.selectBatchIds(docIds).stream().collect(Collectors.toMap(Doctor::getId, Doctor::getName, (a, b) -> a));
        Map<Long, String> userNames = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> mask(u.getUsername()), (a, b) -> a));
        return list.stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId());
            vo.setOrderType(r.getOrderType());
            vo.setOrderId(r.getOrderId());
            vo.setUserId(r.getUserId());
            vo.setUserName(userNames.get(r.getUserId()));
            vo.setDoctorId(r.getDoctorId());
            vo.setDoctorName(docNames.get(r.getDoctorId()));
            vo.setRating(r.getRating());
            vo.setContent(r.getContent());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /** 用户名脱敏：保留首尾字符 */
    private String mask(String name) {
        if (name == null || name.isEmpty()) return "匿名用户";
        if (name.length() == 1) return name + "*";
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }
}
