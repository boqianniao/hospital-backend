package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.Result;
import com.hospital.third.oss.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "文件", description = "文件上传到阿里云 OSS（需登录）")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final OssService ossService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dir", required = false) String dir) {
        UserContext.requireUserId();
        String url = ossService.upload(file, dir);
        return Result.success("上传成功", Map.of("url", url));
    }
}
