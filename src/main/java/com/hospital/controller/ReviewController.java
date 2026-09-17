package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.dto.ReviewDTO;
import com.hospital.service.ReviewService;
import com.hospital.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价", description = "提交评价、医生评价列表、我的评价")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "提交评价（需登录）")
    @PostMapping
    public Result<ReviewVO> submit(@Valid @RequestBody ReviewDTO dto) {
        return Result.success("评价成功", reviewService.submit(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "医生评价列表（公开）")
    @GetMapping("/doctor/{doctorId}")
    public Result<PageResult<ReviewVO>> byDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(reviewService.listByDoctor(doctorId, pageNum, pageSize));
    }

    @Operation(summary = "我的评价（需登录）")
    @GetMapping("/mine")
    public Result<PageResult<ReviewVO>> mine(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(reviewService.myReviews(UserContext.requireUserId(), pageNum, pageSize));
    }
}
