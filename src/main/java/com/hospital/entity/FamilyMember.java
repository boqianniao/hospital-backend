package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 就诊成员表 t_family_member */
@Data
@TableName("t_family_member")
public class FamilyMember implements Serializable {

    @TableId
    private Long id;
    /** 户主用户ID */
    private Long userId;
    private String name;
    /** 性别: 1男 2女 */
    private Integer gender;
    private LocalDate birthday;
    private String phone;
    private String idCard;
    /** 与本人关系 1本人 2配偶 3父母 4子女 5兄弟姐妹 6其他 */
    private String relation;
    /** 是否默认: 1是 0否 */
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
