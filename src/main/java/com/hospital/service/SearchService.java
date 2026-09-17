package com.hospital.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.result.PageResult;
import com.hospital.config.props.HospitalProperties;
import com.hospital.entity.Article;
import com.hospital.entity.Disease;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import com.hospital.mapper.ArticleMapper;
import com.hospital.mapper.DiseaseMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.HospitalMapper;
import com.hospital.search.doc.ArticleDoc;
import com.hospital.search.doc.DiseaseDoc;
import com.hospital.search.doc.DoctorDoc;
import com.hospital.search.doc.HospitalDoc;
import com.hospital.vo.SearchOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 全文搜索：优先走 Elasticsearch，ES 不可用（探活失败/查询异常）时自动降级为数据库 LIKE 查询。
 * ES 只存可检索字段并返回命中 id，明细统一回源数据库，保证展示数据一致。
 * 每次有效搜索会累加热门搜索计数。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final String[] HOSPITAL_FIELDS = {"name", "intro", "address", "city"};
    private static final String[] DOCTOR_FIELDS = {"name", "title", "intro", "expertise"};
    private static final String[] DISEASE_FIELDS = {"name", "alias", "description", "symptoms"};
    private static final String[] ARTICLE_FIELDS = {"title", "summary", "content", "author"};

    private final ElasticsearchOperations es;
    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;
    private final DiseaseMapper diseaseMapper;
    private final ArticleMapper articleMapper;
    private final SearchHistoryService searchHistoryService;
    private final HospitalProperties props;

    /** ES 可用性缓存：null=未探测，true/false=已探测结果 */
    private volatile Boolean esUp;
    /** ES 判定不可用的时间戳，用于按 TTL 自动重探 */
    private volatile long esDownAtMs;

    // ---------------- 对外搜索 ----------------

    public PageResult<Hospital> searchHospitals(String keyword, long pageNum, long pageSize) {
        return searchHospitals(keyword, pageNum, pageSize, true);
    }

    public PageResult<Hospital> searchHospitals(String keyword, long pageNum, long pageSize, boolean track) {
        if (track) {
            searchHistoryService.incrHot(keyword);
        }
        if (StringUtils.hasText(keyword) && esUsable()) {
            try {
                EsPage ep = esSearch(HospitalDoc.class, keyword, HOSPITAL_FIELDS, pageNum, pageSize, HospitalDoc::getId);
                return PageResult.of(loadByIds(hospitalMapper, ep.ids, Hospital::getId), ep.total, pageNum, pageSize);
            } catch (Exception e) {
                markEsDown("hospital", e);
            }
        }
        return dbHospitals(keyword, pageNum, pageSize);
    }

    public PageResult<Doctor> searchDoctors(String keyword, long pageNum, long pageSize) {
        return searchDoctors(keyword, pageNum, pageSize, true);
    }

    public PageResult<Doctor> searchDoctors(String keyword, long pageNum, long pageSize, boolean track) {
        if (track) {
            searchHistoryService.incrHot(keyword);
        }
        if (StringUtils.hasText(keyword)) {
            IPage<Doctor> nameMatches = doctorMapper.selectPage(new Page<>(pageNum, pageSize),
                    Wrappers.<Doctor>lambdaQuery()
                            .like(Doctor::getName, keyword.trim())
                            .orderByDesc(Doctor::getConsultCount));
            if (nameMatches.getTotal() > 0) {
                return PageResult.of(nameMatches);
            }
        }
        if (StringUtils.hasText(keyword) && esUsable()) {
            try {
                EsPage ep = esSearch(DoctorDoc.class, keyword, DOCTOR_FIELDS, pageNum, pageSize, DoctorDoc::getId);
                return PageResult.of(loadByIds(doctorMapper, ep.ids, Doctor::getId), ep.total, pageNum, pageSize);
            } catch (Exception e) {
                markEsDown("doctor", e);
            }
        }
        return dbDoctors(keyword, pageNum, pageSize);
    }

    public PageResult<Disease> searchDiseases(String keyword, long pageNum, long pageSize) {
        return searchDiseases(keyword, pageNum, pageSize, true);
    }

    public PageResult<Disease> searchDiseases(String keyword, long pageNum, long pageSize, boolean track) {
        if (track) {
            searchHistoryService.incrHot(keyword);
        }
        if (StringUtils.hasText(keyword) && esUsable()) {
            try {
                EsPage ep = esSearch(DiseaseDoc.class, keyword, DISEASE_FIELDS, pageNum, pageSize, DiseaseDoc::getId);
                return PageResult.of(loadByIds(diseaseMapper, ep.ids, Disease::getId), ep.total, pageNum, pageSize);
            } catch (Exception e) {
                markEsDown("disease", e);
            }
        }
        return dbDiseases(keyword, pageNum, pageSize);
    }

    public PageResult<Article> searchArticles(String keyword, long pageNum, long pageSize) {
        return searchArticles(keyword, pageNum, pageSize, true);
    }

    public PageResult<Article> searchArticles(String keyword, long pageNum, long pageSize, boolean track) {
        if (track) {
            searchHistoryService.incrHot(keyword);
        }
        if (StringUtils.hasText(keyword) && esUsable()) {
            try {
                EsPage ep = esSearch(ArticleDoc.class, keyword, ARTICLE_FIELDS, pageNum, pageSize, ArticleDoc::getId);
                return PageResult.of(loadByIds(articleMapper, ep.ids, Article::getId), ep.total, pageNum, pageSize);
            } catch (Exception e) {
                markEsDown("article", e);
            }
        }
        return dbArticles(keyword, pageNum, pageSize);
    }

    /** 一次搜索同时返回四类结果，热搜只计数一次。 */
    public SearchOverviewVO searchAll(String keyword, long pageNum, long pageSize) {
        searchHistoryService.incrHot(keyword);
        return new SearchOverviewVO(
                searchHospitals(keyword, pageNum, pageSize, false),
                searchDoctors(keyword, pageNum, pageSize, false),
                searchDiseases(keyword, pageNum, pageSize, false),
                searchArticles(keyword, pageNum, pageSize, false));
    }

    // ---------------- 重建索引 ----------------

    /** 从数据库全量重建 ES 索引；ES 不可用时返回错误信息但不抛出。 */
    public Map<String, Object> reindex() {
        esUp = null; // 触发重新探活
        if (!esUsable()) {
            return Map.of("success", false, "message", "Elasticsearch 不可用，未执行重建");
        }
        try {
            int h = index(HospitalDoc.class,
                    hospitalMapper.selectList(null).stream().map(this::toDoc).collect(Collectors.toList()));
            int d = index(DoctorDoc.class,
                    doctorMapper.selectList(null).stream().map(this::toDoc).collect(Collectors.toList()));
            int s = index(DiseaseDoc.class,
                    diseaseMapper.selectList(null).stream().map(this::toDoc).collect(Collectors.toList()));
            int a = index(ArticleDoc.class,
                    articleMapper.selectList(null).stream().map(this::toDoc).collect(Collectors.toList()));
            return Map.of("success", true, "hospital", h, "doctor", d, "disease", s, "article", a);
        } catch (Exception e) {
            markEsDownState();
            log.error("重建 ES 索引失败: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "重建失败: " + e.getMessage());
        }
    }

    // ---------------- ES 内部 ----------------

    private <D> EsPage esSearch(Class<D> clazz, String keyword, String[] fields,
                                long pageNum, long pageSize, Function<D, Long> idFn) {
        Criteria criteria = null;
        for (String f : fields) {
            Criteria fc = new Criteria(f).matches(keyword);
            criteria = (criteria == null) ? fc : criteria.or(fc);
        }
        CriteriaQuery query = new CriteriaQuery(criteria);
        int page = (int) Math.max(1, pageNum);
        int size = (int) Math.max(1, pageSize);
        query.setPageable(PageRequest.of(page - 1, size));
        SearchHits<D> hits = es.search(query, clazz);
        List<Long> ids = hits.getSearchHits().stream()
                .map(h -> idFn.apply(h.getContent()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new EsPage(ids, hits.getTotalHits());
    }

    private <D> int index(Class<D> clazz, List<D> docs) {
        IndexOperations io = es.indexOps(clazz);
        if (!io.exists()) {
            io.createWithMapping();
        }
        if (docs.isEmpty()) {
            return 0;
        }
        es.save(docs);
        return docs.size();
    }

    private boolean esUsable() {
        Boolean up = esUp;
        // 已判定不可用，但超过重探间隔则清空状态，下面重新探活（ES 恢复后无需重启即可自愈）
        if (Boolean.FALSE.equals(up) && System.currentTimeMillis() - esDownAtMs > props.getSearch().getEsRetryIntervalMs()) {
            esUp = null;
            up = null;
        }
        if (up != null) {
            return up;
        }
        synchronized (this) {
            if (esUp != null) {
                return esUp;
            }
            try {
                es.indexOps(HospitalDoc.class).exists();
                esUp = Boolean.TRUE;
            } catch (Exception e) {
                markEsDownState();
                log.warn("ES 探活失败，搜索降级为数据库查询: {}", e.getMessage());
            }
            return esUp;
        }
    }

    private void markEsDown(String index, Exception e) {
        markEsDownState();
        log.warn("ES 查询[{}]失败，本次降级数据库: {}", index, e.getMessage());
    }

    private void markEsDownState() {
        esUp = Boolean.FALSE;
        esDownAtMs = System.currentTimeMillis();
    }

    /** 按 ES 命中的 id 顺序回源数据库并保持排序 */
    private <T> List<T> loadByIds(BaseMapper<T> mapper, List<Long> ids, Function<T, Long> idFn) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, T> map = mapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(idFn, Function.identity(), (x, y) -> x));
        return ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ---------------- DB 降级查询 ----------------

    private PageResult<Hospital> dbHospitals(String kw, long pageNum, long pageSize) {
        IPage<Hospital> p = hospitalMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Hospital>lambdaQuery()
                        .eq(Hospital::getStatus, 1)
                        .and(StringUtils.hasText(kw), w -> w
                                .like(Hospital::getName, kw).or()
                                .like(Hospital::getIntro, kw).or()
                                .like(Hospital::getAddress, kw).or()
                                .like(Hospital::getCity, kw))
                        .orderByDesc(Hospital::getFollowCount));
        return PageResult.of(p);
    }

    private PageResult<Doctor> dbDoctors(String kw, long pageNum, long pageSize) {
        IPage<Doctor> p = doctorMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Doctor>lambdaQuery()
                        .eq(Doctor::getStatus, 1)
                        .and(StringUtils.hasText(kw), w -> w
                                .like(Doctor::getName, kw).or()
                                .like(Doctor::getTitle, kw).or()
                                .like(Doctor::getIntro, kw).or()
                                .like(Doctor::getExpertise, kw))
                        .orderByDesc(Doctor::getConsultCount));
        return PageResult.of(p);
    }

    private PageResult<Disease> dbDiseases(String kw, long pageNum, long pageSize) {
        IPage<Disease> p = diseaseMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Disease>lambdaQuery()
                        .and(StringUtils.hasText(kw), w -> w
                                .like(Disease::getName, kw).or()
                                .like(Disease::getAlias, kw).or()
                                .like(Disease::getDescription, kw).or()
                                .like(Disease::getSymptoms, kw))
                        .orderByDesc(Disease::getFollowCount));
        return PageResult.of(p);
    }

    private PageResult<Article> dbArticles(String kw, long pageNum, long pageSize) {
        IPage<Article> p = articleMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Article>lambdaQuery()
                        .eq(Article::getStatus, 1)
                        .and(StringUtils.hasText(kw), w -> w
                                .like(Article::getTitle, kw).or()
                                .like(Article::getSummary, kw).or()
                                .like(Article::getContent, kw).or()
                                .like(Article::getAuthor, kw))
                        .orderByDesc(Article::getViews));
        return PageResult.of(p);
    }

    // ---------------- entity -> doc ----------------

    private HospitalDoc toDoc(Hospital h) {
        HospitalDoc d = new HospitalDoc();
        d.setId(h.getId());
        d.setName(h.getName());
        d.setIntro(h.getIntro());
        d.setAddress(h.getAddress());
        d.setCity(h.getCity());
        d.setLevel(h.getLevel());
        d.setFollowCount(h.getFollowCount());
        return d;
    }

    private DoctorDoc toDoc(Doctor src) {
        DoctorDoc d = new DoctorDoc();
        d.setId(src.getId());
        d.setName(src.getName());
        d.setTitle(src.getTitle());
        d.setIntro(src.getIntro());
        d.setExpertise(src.getExpertise());
        d.setConsultCount(src.getConsultCount());
        return d;
    }

    private DiseaseDoc toDoc(Disease src) {
        DiseaseDoc d = new DiseaseDoc();
        d.setId(src.getId());
        d.setName(src.getName());
        d.setAlias(src.getAlias());
        d.setDescription(src.getDescription());
        d.setSymptoms(src.getSymptoms());
        d.setFollowCount(src.getFollowCount());
        return d;
    }

    private ArticleDoc toDoc(Article src) {
        ArticleDoc d = new ArticleDoc();
        d.setId(src.getId());
        d.setTitle(src.getTitle());
        d.setSummary(src.getSummary());
        d.setContent(src.getContent());
        d.setAuthor(src.getAuthor());
        d.setViews(src.getViews());
        return d;
    }

    /** ES 命中结果内部载体 */
    private static class EsPage {
        final List<Long> ids;
        final long total;

        EsPage(List<Long> ids, long total) {
            this.ids = ids;
            this.total = total;
        }
    }
}
