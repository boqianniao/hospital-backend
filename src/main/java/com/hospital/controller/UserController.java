package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.Result;
import com.hospital.dto.auth.ChangePasswordDTO;
import com.hospital.dto.auth.UpdateProfileDTO;
import com.hospital.service.UserService;
import com.hospital.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 当前登录用户资料接口（需登录）。
 */
@Tag(name = "用户", description = "个人资料/修改密码")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.success(userService.getProfile(UserContext.requireUserId()));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody UpdateProfileDTO dto) {
        return Result.success("更新成功", userService.updateProfile(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(UserContext.requireUserId(), dto);
        return Result.success("密码修改成功", null);
    }
}
