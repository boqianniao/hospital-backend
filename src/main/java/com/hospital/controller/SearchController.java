package com.hospital.controller;

import com.hospital.common.context.UserContext;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.Result;
import com.hospital.entity.Article;
import com.hospital.entity.Disease;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.service.SearchHistoryService;
import com.hospital.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "搜索", description = "全文搜索(ES，不可用时降级DB) + 搜索历史 + 热门搜索")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "医院搜索")
    @GetMapping("/hospitals")
    public Result<PageResult<Hospital>> hospitals(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(searchService.searchHospitals(keyword, pageNum, pageSize));
    }

    @Operation(summary = "医生搜索")
    @GetMapping("/doctors")
    public Result<PageResult<Doctor>> doctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(searchService.searchDoctors(keyword, pageNum, pageSize));
    }

    @Operation(summary = "疾病搜索")
    @GetMapping("/diseases")
    public Result<PageResult<Disease>> diseases(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(searchService.searchDiseases(keyword, pageNum, pageSize));
    }

    @Operation(summary = "文章搜索")
    @GetMapping("/articles")
    public Result<PageResult<Article>> articles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(searchService.searchArticles(keyword, pageNum, pageSize));
    }

    @Operation(summary = "热门搜索词（公开）")
    @GetMapping("/hot")
    public Result<List<String>> hot(@RequestParam(required = false) Integer limit) {
        return Result.success(searchHistoryService.hotKeywords(limit));
    }

    @Operation(summary = "我的搜索历史（需登录）")
    @GetMapping("/history")
    public Result<List<String>> history() {
        return Result.success(searchHistoryService.listHistory(UserContext.requireUserId()));
    }

    @Operation(summary = "记录搜索历史（需登录）")
    @PostMapping("/history")
    public Result<Void> addHistory(@RequestParam String keyword) {
        searchHistoryService.addHistory(UserContext.requireUserId(), keyword);
        return Result.success("ok", null);
    }

    @Operation(summary = "清空搜索历史（需登录）")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        searchHistoryService.clearHistory(UserContext.requireUserId());
        return Result.success("已清空", null);
    }

    @Operation(summary = "重建 ES 索引（需登录）")
    @PostMapping("/reindex")
    public Result<Map<String, Object>> reindex() {
        UserContext.requireUserId();
        return Result.success(searchService.reindex());
    }
}
