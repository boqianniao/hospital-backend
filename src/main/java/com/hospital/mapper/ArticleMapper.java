package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /** 阅读量 +1 */
    @Update("UPDATE t_article SET views = views + 1 WHERE id = #{id}")
    int incrViews(Long id);
}
