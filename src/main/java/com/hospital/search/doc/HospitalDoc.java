package com.hospital.search.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/** 医院搜索索引文档 index=hospital */
@Data
@Document(indexName = "hospital")
public class HospitalDoc {

    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String intro;
    @Field(type = FieldType.Text)
    private String address;
    @Field(type = FieldType.Keyword)
    private String city;
    @Field(type = FieldType.Keyword)
    private String level;
    @Field(type = FieldType.Integer)
    private Integer followCount;
}
