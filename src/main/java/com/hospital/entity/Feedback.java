package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 反馈表 t_feedback */
@Data
@TableName("t_feedback")
public class Feedback implements Serializable {

    @TableId
    private Long id;
    private Long userId;
    /** 反馈类型: 1系统问题 2服务问题 3医生问题 4其他问题 */
    private Integer feedbackType;
    private String content;
    /** 反馈图片(逗号分隔) */
    private String images;
    /** 状态: 1待处理 2已处理 */
    private Integer status;
    private String replyContent;
    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
