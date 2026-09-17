package com.hospital.search.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/** 疾病搜索索引文档 index=disease */
@Data
@Document(indexName = "disease")
public class DiseaseDoc {

    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String alias;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Text)
    private String symptoms;
    @Field(type = FieldType.Integer)
    private Integer followCount;
}
