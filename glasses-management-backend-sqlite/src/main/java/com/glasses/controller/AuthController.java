package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.glasses.dto.ChangePasswordDTO;
import com.glasses.dto.LoginDTO;
import com.glasses.dto.ProfileUpdateDTO;
import com.glasses.dto.RegisterDTO;
import com.glasses.service.AuthService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功");
    }

    @SaCheckLogin
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo() {
        return Result.success(authService.getInfo());
    }

    @SaCheckLogin
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success(true);
    }

    @SaCheckLogin
    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        return Result.success(authService.updateProfile(dto));
    }
}
