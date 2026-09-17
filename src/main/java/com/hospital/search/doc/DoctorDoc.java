package com.hospital.search.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/** 医生搜索索引文档 index=doctor */
@Data
@Document(indexName = "doctor")
public class DoctorDoc {

    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String intro;
    @Field(type = FieldType.Text)
    private String expertise;
    @Field(type = FieldType.Integer)
    private Integer consultCount;
}
