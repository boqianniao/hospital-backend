package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchSuggestionServiceTest {

    private final HospitalMapper hospitalMapper = mock(HospitalMapper.class);
    private final DoctorMapper doctorMapper = mock(DoctorMapper.class);
    private final DiseaseMapper diseaseMapper = mock(DiseaseMapper.class);
    private final ArticleMapper articleMapper = mock(ArticleMapper.class);
    private final SearchHistoryService historyService = mock(SearchHistoryService.class);
    private final HospitalProperties properties = new HospitalProperties();
    private SearchSuggestionService service;

    @BeforeEach
    void setUp() {
        service = new SearchSuggestionService(hospitalMapper, doctorMapper, diseaseMapper,
                articleMapper, historyService, properties);
    }

    @Test
    void blankKeywordDoesNotQueryDataSources() {
        assertTrue(service.suggest("  ", 8).isEmpty());
        verify(hospitalMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
        verify(historyService, never()).hotKeywords(any());
    }

    @Test
    void combinesMatchingHotTermsAndTypedBusinessNamesWithinLimit() {
        when(historyService.hotKeywords(50)).thenReturn(List.of("协和医院", "感冒"));
        stubPage(hospitalMapper, hospital("北京协和医院"));
        stubPage(doctorMapper, doctor("协和医生"));
        stubPage(diseaseMapper, disease("协和综合征"));
        stubPage(articleMapper, article("走进协和医院"));

        List<SearchSuggestionVO> result = service.suggest("协和", 4);

        assertEquals(4, result.size());
        assertEquals("协和医院", result.get(0).getKeyword());
        assertEquals("all", result.get(0).getType());
        assertEquals(List.of("医院", "医生", "疾病"),
                result.subList(1, 4).stream().map(SearchSuggestionVO::getTypeName).toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void stubPage(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, T record) {
        when(mapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<T> page = invocation.getArgument(0);
            page.setRecords(List.of(record));
            return page;
        });
    }

    private Hospital hospital(String name) {
        Hospital item = new Hospital();
        item.setName(name);
        return item;
    }

    private Doctor doctor(String name) {
        Doctor item = new Doctor();
        item.setName(name);
        return item;
    }

    private Disease disease(String name) {
        Disease item = new Disease();
        item.setName(name);
        return item;
    }

    private Article article(String title) {
        Article item = new Article();
        item.setTitle(title);
        return item;
    }
}
