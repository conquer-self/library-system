package com.library.dto;

import lombok.Data;



/**
 * 用户注册数据传输对象
 */
@Data
public class RegisterDTO {
    // 用户名/学号
    private String username;
    // 密码
    private String password;
    // 真实姓名
    private String RealName;
    // 手机号
    private String phone;
    // 新增邮箱字段
    private String email;
}
