package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.dto.FollowDTO;
import com.hospital.service.FollowService;
import com.hospital.vo.FollowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "关注", description = "关注/取消关注医院·医生·疾病，我的关注（需登录）")
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "关注")
    @PostMapping
    public Result<Void> follow(@Valid @RequestBody FollowDTO dto) {
        followService.follow(UserContext.requireUserId(), dto.getFollowType(), dto.getFollowId());
        return Result.success("关注成功", null);
    }

    @Operation(summary = "取消关注")
    @DeleteMapping
    public Result<Void> unfollow(@RequestParam Integer followType, @RequestParam Long followId) {
        followService.unfollow(UserContext.requireUserId(), followType, followId);
        return Result.success("已取消关注", null);
    }

    @Operation(summary = "是否已关注")
    @GetMapping("/status")
    public Result<Boolean> status(@RequestParam Integer followType, @RequestParam Long followId) {
        return Result.success(followService.isFollowing(UserContext.requireUserId(), followType, followId));
    }

    @Operation(summary = "我的关注列表")
    @GetMapping
    public Result<PageResult<FollowVO>> myFollows(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer followType) {
        return Result.success(followService.myFollows(UserContext.requireUserId(), followType, pageNum, pageSize));
    }
}
