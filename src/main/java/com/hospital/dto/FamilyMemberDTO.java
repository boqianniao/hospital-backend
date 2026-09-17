package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/** 就诊成员新增/编辑 */
@Data
public class FamilyMemberDTO {

    @NotBlank(message = "姓名不能为空")
    private String name;
    /** 性别: 1男 2女 */
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String phone;
    private String idCard;
    /** 与本人关系 1本人 2配偶 3父母 4子女 5兄弟姐妹 6其他 */
    @NotBlank(message = "与本人关系不能为空")
    private String relation;
    /** 是否设为默认: 1是 0否 */
    private Integer isDefault = 0;
}
