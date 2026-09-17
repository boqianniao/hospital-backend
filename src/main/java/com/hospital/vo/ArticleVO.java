package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 文章信息（列表不含 content，详情含 content） */
@Data
public class ArticleVO implements Serializable {

    private Long id;
    private String title;
    private String summary;
    /** 富文本内容，仅详情返回 */
    private String content;
    private Long departmentId;
    private String departmentName;
    private String author;
    private String image;
    private Integer views;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
