package com.hospital.controller;

import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.service.DoctorService;
import com.hospital.vo.DoctorVO;
import com.hospital.vo.ScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "医生", description = "医生列表/详情/排班")
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "医生分页列表")
    @GetMapping
    public Result<PageResult<DoctorVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword) {
        return Result.success(doctorService.page(pageNum, pageSize, hospitalId, departmentId, keyword));
    }

    @Operation(summary = "医生详情")
    @GetMapping("/{id}")
    public Result<DoctorVO> detail(@PathVariable Long id) {
        return Result.success(doctorService.detail(id));
    }

    @Operation(summary = "医生排班（号源）")
    @GetMapping("/{id}/schedules")
    public Result<List<ScheduleVO>> schedules(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Result.success(doctorService.schedules(id, from, to));
    }
}
