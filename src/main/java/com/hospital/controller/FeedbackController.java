package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.dto.FeedbackDTO;
import com.hospital.entity.Feedback;
import com.hospital.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "意见反馈", description = "提交反馈、我的反馈（需登录）")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "提交反馈")
    @PostMapping
    public Result<Feedback> submit(@Valid @RequestBody FeedbackDTO dto) {
        return Result.success("提交成功", feedbackService.submit(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "我的反馈列表")
    @GetMapping
    public Result<PageResult<Feedback>> myPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(feedbackService.myPage(UserContext.requireUserId(), pageNum, pageSize));
    }
}
