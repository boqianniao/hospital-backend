package com.hospital.controller;

import com.hospital.common.result.Result;
import com.hospital.config.props.HospitalProperties;
import com.hospital.dto.auth.LoginDTO;
import com.hospital.dto.auth.RegisterDTO;
import com.hospital.dto.auth.SendCodeDTO;
import com.hospital.service.AuthService;
import com.hospital.service.VerifyCodeService;
import com.hospital.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口：验证码 / 注册 / 登录 / 登出。均在鉴权白名单内（登出除外仍可匿名调用）。
 */
@Tag(name = "认证", description = "验证码/注册/登录/登出")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerifyCodeService verifyCodeService;
    private final HospitalProperties props;

    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        String scene = StringUtils.hasText(dto.getScene()) ? dto.getScene() : "register";
        verifyCodeService.sendCode(dto.getPhone(), scene);
        return Result.success("验证码已发送", null);
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功", null);
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader(props.getJwt().getHeader()));
        return Result.success("已退出登录", null);
    }
}
