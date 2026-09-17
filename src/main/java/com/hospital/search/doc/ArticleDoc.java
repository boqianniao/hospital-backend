package com.hospital.search.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/** 健康科普文章搜索索引文档 index=article */
@Data
@Document(indexName = "article")
public class ArticleDoc {

    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String summary;
    @Field(type = FieldType.Text)
    private String content;
    @Field(type = FieldType.Keyword)
    private String author;
    @Field(type = FieldType.Integer)
    private Integer views;
}
