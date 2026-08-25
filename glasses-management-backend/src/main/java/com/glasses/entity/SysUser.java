package com.glasses.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

@Data
@Table("sys_user")
public class SysUser {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String realName;
    private String role;
    private Boolean mustChangePassword;
    private Boolean disabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date disabledTime;
    @Column(isLogicDelete = true)
    private Boolean deleted = false;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deletedTime;
    /** 强制重置密码执行时间；非空表示 force-reset 开关已被消费，防止重复执行 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date forceResetTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}