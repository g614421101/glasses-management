package com.glasses.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String phone;
    private String password;
    private String confirmPassword;
    private String inviteCode;
}
