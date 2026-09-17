package com.hospital.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MyBatis-Plus 配置：分页插件 + 公共字段（创建/更新时间）自动填充。
 */
@Configuration
public class MybatisPlusConfig {

    /** 单页最大条数，防止恶意/异常的超大分页 */
    private static final long MAX_PAGE_SIZE = 100L;
    /** 非法 pageSize（<=0）时使用的默认条数 */
    private static final long DEFAULT_PAGE_SIZE = 10L;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 先钳制分页参数，避免 pageSize<=0 关闭分页返回整表、或超大 size 拖垮数据库
        interceptor.addInnerInterceptor(new PageSanitizeInnerInterceptor());
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        page.setMaxLimit(MAX_PAGE_SIZE);
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }

    /**
     * 分页参数清洗：在真正分页前把非法的 current/size 收敛到合理区间。
     * current < 1 → 1；size <= 0 → 默认 10；size > 100 → 100。覆盖所有走 IPage 的查询。
     */
    static class PageSanitizeInnerInterceptor implements InnerInterceptor {
        @Override
        public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                                RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
            IPage<?> page = extractPage(parameter);
            if (page == null) {
                return;
            }
            if (page.getCurrent() < 1) {
                page.setCurrent(1);
            }
            long size = page.getSize();
            if (size <= 0) {
                page.setSize(DEFAULT_PAGE_SIZE);
            } else if (size > MAX_PAGE_SIZE) {
                page.setSize(MAX_PAGE_SIZE);
            }
        }

        private static IPage<?> extractPage(Object parameter) {
            if (parameter instanceof IPage) {
                return (IPage<?>) parameter;
            }
            if (parameter instanceof Map) {
                for (Object v : ((Map<?, ?>) parameter).values()) {
                    if (v instanceof IPage) {
                        return (IPage<?>) v;
                    }
                }
            }
            return null;
        }
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
