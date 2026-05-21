package com.glasses.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String realName;
    private String role;
    private Boolean mustChangePassword;
    private Boolean disabled;
    private Date disabledTime;
    @TableLogic
    private Boolean deleted = false;
    private Date deletedTime;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}