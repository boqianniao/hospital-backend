package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 健康科普文章表 t_article */
@Data
@TableName("t_article")
public class Article implements Serializable {

    @TableId
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long departmentId;
    private String author;
    private String image;
    private Integer views;
    /** 状态: 1已发布 0草稿 */
    private Integer status;
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
