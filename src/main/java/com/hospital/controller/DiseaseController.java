package com.hospital.controller;

import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.service.DiseaseService;
import com.hospital.vo.DiseaseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "疾病", description = "疾病百科列表/详情")
@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    @Operation(summary = "疾病分页列表")
    @GetMapping
    public Result<PageResult<DiseaseVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword) {
        return Result.success(diseaseService.page(pageNum, pageSize, departmentId, keyword));
    }

    @Operation(summary = "疾病详情")
    @GetMapping("/{id}")
    public Result<DiseaseVO> detail(@PathVariable Long id) {
        return Result.success(diseaseService.detail(id));
    }
}
