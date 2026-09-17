package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.entity.Article;
import com.hospital.mapper.ArticleMapper;
import com.hospital.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章服务：分页/筛选、详情（阅读量+1）。
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final DepartmentService departmentService;

    /** 列表：仅已发布，不返回 content */
    public PageResult<ArticleVO> page(long pageNum, long pageSize, Long departmentId, String keyword) {
        LambdaQueryWrapper<Article> qw = Wrappers.<Article>lambdaQuery()
                .select(Article::getId, Article::getTitle, Article::getSummary, Article::getDepartmentId,
                        Article::getAuthor, Article::getImage, Article::getViews, Article::getStatus,
                        Article::getPublishTime)
                .eq(Article::getStatus, 1)
                .eq(departmentId != null, Article::getDepartmentId, departmentId)
                .like(StringUtils.hasText(keyword), Article::getTitle, keyword)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId);
        IPage<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return PageResult.of(page, enrich(page.getRecords()));
    }

    /** 详情：含 content，阅读量+1 */
    public ArticleVO detail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        articleMapper.incrViews(id);
        article.setViews((article.getViews() == null ? 0 : article.getViews()) + 1);
        return enrich(Collections.singletonList(article)).get(0);
    }

    private List<ArticleVO> enrich(List<Article> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> deptIds = list.stream().map(Article::getDepartmentId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        Map<Long, String> deptNames = departmentService.nameMap(deptIds);
        return list.stream().map(a -> {
            ArticleVO vo = new ArticleVO();
            BeanUtil.copyProperties(a, vo);
            vo.setDepartmentName(deptNames.get(a.getDepartmentId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
