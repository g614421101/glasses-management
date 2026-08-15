package com.glasses.dto;

import lombok.Data;

/**
 * 系统初始化（创建管理员）请求。
 */
@Data
public class SetupDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String inviteCode;
}
