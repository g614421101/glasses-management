package com.glasses.entity;

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
    private Date disabledTime;
    @Column(isLogicDelete = true)
    private Boolean deleted = false;
    private Date deletedTime;
    private Date createTime;
}