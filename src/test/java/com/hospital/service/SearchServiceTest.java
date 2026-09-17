package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.config.props.HospitalProperties;
import com.hospital.mapper.ArticleMapper;
import com.hospital.mapper.DiseaseMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.HospitalMapper;
import com.hospital.vo.SearchOverviewVO;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void allSearchTracksKeywordOnlyOnce() {
        ElasticsearchOperations elasticsearch = mock(ElasticsearchOperations.class);
        HospitalMapper hospitalMapper = mock(HospitalMapper.class);
        DoctorMapper doctorMapper = mock(DoctorMapper.class);
        DiseaseMapper diseaseMapper = mock(DiseaseMapper.class);
        ArticleMapper articleMapper = mock(ArticleMapper.class);
        SearchHistoryService historyService = mock(SearchHistoryService.class);

        when(hospitalMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(i -> i.getArgument(0));
        when(doctorMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(i -> i.getArgument(0));
        when(diseaseMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(i -> i.getArgument(0));
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(i -> i.getArgument(0));

        SearchService service = new SearchService(elasticsearch, hospitalMapper, doctorMapper,
                diseaseMapper, articleMapper, historyService, new HospitalProperties());

        SearchOverviewVO result = service.searchAll("", 1, 10);

        assertNotNull(result.getHospitals());
        assertNotNull(result.getDoctors());
        assertNotNull(result.getDiseases());
        assertNotNull(result.getArticles());
        verify(historyService).incrHot("");
    }
}
