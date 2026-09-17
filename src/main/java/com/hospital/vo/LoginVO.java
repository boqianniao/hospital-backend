package com.hospital.vo;

import lombok.Data;

/** 登录响应：token + 用户信息 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;
}
