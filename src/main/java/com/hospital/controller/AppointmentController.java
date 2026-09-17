package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.dto.order.AppointmentCreateDTO;
import com.hospital.service.AppointmentService;
import com.hospital.vo.AppointmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "挂号订单", description = "预约挂号下单/我的挂号/取消（需登录）")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "预约挂号下单")
    @PostMapping
    public Result<AppointmentVO> create(@Valid @RequestBody AppointmentCreateDTO dto) {
        return Result.success("下单成功", appointmentService.create(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "我的挂号列表")
    @GetMapping
    public Result<PageResult<AppointmentVO>> myPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(appointmentService.myPage(UserContext.requireUserId(), pageNum, pageSize, status));
    }

    @Operation(summary = "挂号订单详情")
    @GetMapping("/{id}")
    public Result<AppointmentVO> detail(@PathVariable Long id) {
        return Result.success(appointmentService.detail(UserContext.requireUserId(), id));
    }

    @Operation(summary = "取消挂号（退款+释放号源）")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(UserContext.requireUserId(), id);
        return Result.success("已取消", null);
    }

    @Operation(summary = "完成挂号")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        appointmentService.complete(UserContext.requireUserId(), id);
        return Result.success("已完成", null);
    }
}
