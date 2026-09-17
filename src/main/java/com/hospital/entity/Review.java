package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 评价表 t_review */
@Data
@TableName("t_review")
public class Review implements Serializable {

    @TableId
    private Long id;
    /** 订单类型: 1挂号 2咨询 */
    private Integer orderType;
    private Long orderId;
    private Long userId;
    private Long doctorId;
    /** 评分(1-5) */
    private Integer rating;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
