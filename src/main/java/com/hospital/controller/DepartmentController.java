package com.hospital.controller;

import com.hospital.common.result.Result;
import com.hospital.service.DepartmentService;
import com.hospital.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "科室", description = "科室树")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "全部科室树（一级+二级）")
    @GetMapping("/tree")
    public Result<List<DepartmentVO>> tree() {
        return Result.success(departmentService.tree());
    }
}
