package com.glasses.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String phone;
    private String password;
    private String inviteCode;
}
