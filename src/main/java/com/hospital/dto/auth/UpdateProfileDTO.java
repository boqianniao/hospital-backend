package com.hospital.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/** 更新个人资料（仅更新非空字段） */
@Data
public class UpdateProfileDTO {

    private String username;
    private String realName;
    private String email;
    /** 性别: 1男 2女 */
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String avatar;
}
