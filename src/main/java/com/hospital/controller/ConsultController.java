package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.dto.order.ConsultCreateDTO;
import com.hospital.service.ConsultService;
import com.hospital.vo.ConsultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "咨询订单", description = "电话咨询下单/我的咨询/取消（需登录）")
@RestController
@RequestMapping("/api/consults")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    @Operation(summary = "电话咨询下单")
    @PostMapping
    public Result<ConsultVO> create(@Valid @RequestBody ConsultCreateDTO dto) {
        return Result.success("下单成功", consultService.create(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "我的咨询列表")
    @GetMapping
    public Result<PageResult<ConsultVO>> myPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(consultService.myPage(UserContext.requireUserId(), pageNum, pageSize, status));
    }

    @Operation(summary = "咨询订单详情")
    @GetMapping("/{id}")
    public Result<ConsultVO> detail(@PathVariable Long id) {
        return Result.success(consultService.detail(UserContext.requireUserId(), id));
    }

    @Operation(summary = "取消咨询（退款）")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        consultService.cancel(UserContext.requireUserId(), id);
        return Result.success("已取消", null);
    }

    @Operation(summary = "完成咨询")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        consultService.complete(UserContext.requireUserId(), id);
        return Result.success("已完成", null);
    }
}
