package com.hospital.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/** 用户信息（不含密码） */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String realName;
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String avatar;
    private Integer status;
}
