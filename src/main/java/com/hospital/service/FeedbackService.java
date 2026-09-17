package com.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.result.PageResult;
import com.hospital.dto.FeedbackDTO;
import com.hospital.entity.Feedback;
import com.hospital.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 意见反馈：提交反馈、我的反馈列表。
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    /** 反馈状态: 1待处理 2已处理 */
    private static final int STATUS_PENDING = 1;

    private final FeedbackMapper feedbackMapper;

    public Feedback submit(Long userId, FeedbackDTO dto) {
        Feedback fb = new Feedback();
        fb.setUserId(userId);
        fb.setFeedbackType(dto.getFeedbackType());
        fb.setContent(dto.getContent());
        fb.setImages(dto.getImages());
        fb.setStatus(STATUS_PENDING);
        feedbackMapper.insert(fb);
        return fb;
    }

    public PageResult<Feedback> myPage(Long userId, long pageNum, long pageSize) {
        IPage<Feedback> page = feedbackMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Feedback>lambdaQuery()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getId));
        return PageResult.of(page);
    }
}
