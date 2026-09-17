package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.Result;
import com.hospital.dto.FamilyMemberDTO;
import com.hospital.entity.FamilyMember;
import com.hospital.service.FamilyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "就诊成员", description = "就诊成员增删改查/设默认（需登录）")
@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService service;

    @Operation(summary = "我的就诊成员列表")
    @GetMapping
    public Result<List<FamilyMember>> list() {
        return Result.success(service.list(UserContext.requireUserId()));
    }

    @Operation(summary = "新增就诊成员")
    @PostMapping
    public Result<FamilyMember> add(@Valid @RequestBody FamilyMemberDTO dto) {
        return Result.success("添加成功", service.add(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "编辑就诊成员")
    @PutMapping("/{id}")
    public Result<FamilyMember> update(@PathVariable Long id, @Valid @RequestBody FamilyMemberDTO dto) {
        return Result.success("更新成功", service.update(UserContext.requireUserId(), id, dto));
    }

    @Operation(summary = "删除就诊成员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.requireUserId(), id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "设为默认就诊成员")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        service.setDefault(UserContext.requireUserId(), id);
        return Result.success("设置成功", null);
    }
}
