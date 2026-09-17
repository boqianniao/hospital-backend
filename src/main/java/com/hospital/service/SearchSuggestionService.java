package com.hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hospital.config.props.HospitalProperties;
import com.hospital.entity.Article;
import com.hospital.entity.Disease;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.mapper.ArticleMapper;
import com.hospital.mapper.DiseaseMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.HospitalMapper;
import com.hospital.vo.SearchSuggestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索联想词。优先使用匹配的热搜词，再从四类业务数据中补足。
 * 联想查询只返回名称/标题，不返回正文内容，避免下拉面板过重。
 */
@Service
@RequiredArgsConstructor
public class SearchSuggestionService {

    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;
    private final DiseaseMapper diseaseMapper;
    private final ArticleMapper articleMapper;
    private final SearchHistoryService searchHistoryService;
    private final HospitalProperties props;

    public List<SearchSuggestionVO> suggest(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String kw = keyword.trim();
        HospitalProperties.Search cfg = props.getSearch();
        int max = (limit == null || limit <= 0)
                ? cfg.getSuggestionDefaultLimit()
                : Math.min(limit, cfg.getSuggestionMaxLimit());
        int perType = Math.max(1, (max + 3) / 4);
        Map<String, SearchSuggestionVO> result = new LinkedHashMap<>();

        searchHistoryService.hotKeywords(cfg.getHotMaxLimit()).stream()
                .filter(item -> item.contains(kw))
                .limit(Math.min(2, max))
                .forEach(item -> add(result, new SearchSuggestionVO(item, "all", "热门"), max));

        Page<Hospital> hospitals = hospitalMapper.selectPage(new Page<>(1, perType, false),
                Wrappers.<Hospital>lambdaQuery()
                        .eq(Hospital::getStatus, 1)
                        .like(Hospital::getName, kw)
                        .orderByDesc(Hospital::getFollowCount));
        Page<Doctor> doctors = doctorMapper.selectPage(new Page<>(1, perType, false),
                Wrappers.<Doctor>lambdaQuery()
                        .eq(Doctor::getStatus, 1)
                        .like(Doctor::getName, kw)
                        .orderByDesc(Doctor::getConsultCount));
        Page<Disease> diseases = diseaseMapper.selectPage(new Page<>(1, perType, false),
                Wrappers.<Disease>lambdaQuery()
                        .and(w -> w.like(Disease::getName, kw).or().like(Disease::getAlias, kw))
                        .orderByDesc(Disease::getFollowCount));
        Page<Article> articles = articleMapper.selectPage(new Page<>(1, perType, false),
                Wrappers.<Article>lambdaQuery()
                        .eq(Article::getStatus, 1)
                        .like(Article::getTitle, kw)
                        .orderByDesc(Article::getViews));
        List<List<SearchSuggestionVO>> groups = List.of(
                hospitals.getRecords().stream()
                        .map(item -> new SearchSuggestionVO(item.getName(), "hospital", "医院")).toList(),
                doctors.getRecords().stream()
                        .map(item -> new SearchSuggestionVO(item.getName(), "doctor", "医生")).toList(),
                diseases.getRecords().stream()
                        .map(item -> new SearchSuggestionVO(item.getName(), "disease", "疾病")).toList(),
                articles.getRecords().stream()
                        .map(item -> new SearchSuggestionVO(item.getTitle(), "article", "科普")).toList());
        for (int index = 0; index < perType && result.size() < max; index++) {
            for (List<SearchSuggestionVO> group : groups) {
                if (index < group.size()) {
                    add(result, group.get(index), max);
                }
            }
        }

        return new ArrayList<>(result.values());
    }

    private void add(Map<String, SearchSuggestionVO> result, SearchSuggestionVO item, int max) {
        if (result.size() >= max || !StringUtils.hasText(item.getKeyword())) {
            return;
        }
        result.putIfAbsent(item.getType() + ':' + item.getKeyword(), item);
    }
}
