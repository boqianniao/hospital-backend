package com.hospital.controller;

import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.entity.Hospital;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.HospitalService;
import com.hospital.vo.DepartmentVO;
import com.hospital.vo.DoctorVO;
import com.hospital.vo.HospitalDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医院", description = "医院列表/详情/科室/医生")
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;

    @Operation(summary = "医院分页列表")
    @GetMapping
    public Result<PageResult<Hospital>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String level) {
        return Result.success(hospitalService.page(pageNum, pageSize, keyword, province, city, level));
    }

    @Operation(summary = "医院详情（含科室）")
    @GetMapping("/{id}")
    public Result<HospitalDetailVO> detail(@PathVariable Long id) {
        return Result.success(hospitalService.detail(id));
    }

    @Operation(summary = "医院的科室树")
    @GetMapping("/{id}/departments")
    public Result<List<DepartmentVO>> departments(@PathVariable Long id) {
        return Result.success(departmentService.treeByHospital(id));
    }

    @Operation(summary = "医院的医生列表")
    @GetMapping("/{id}/doctors")
    public Result<PageResult<DoctorVO>> doctors(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword) {
        return Result.success(doctorService.page(pageNum, pageSize, id, departmentId, keyword));
    }
}
