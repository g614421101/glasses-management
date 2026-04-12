package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.glasses.dto.LoginDTO;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @org.springframework.beans.factory.annotation.Value("${app.invite-code}")
    private String inviteCode;

    @Autowired
    private SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));
        
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 使用 Sa-Token 登录
        StpUtil.login(user.getId());
        // 将附加信息存入 Session
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());
        StpUtil.getSession().set("role", user.getRole());

        String token = StpUtil.getTokenValue();
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());

        return Result.success(data);
    }
    
    @PostMapping("/register")
    public Result<String> register(@RequestBody com.glasses.dto.RegisterDTO dto) {
        if (!inviteCode.equals(dto.getInviteCode())) {
            return Result.error("邀请码不正确");
        }
        
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getPhone()));
        if (exist != null) {
            return Result.error("该手机号已注册");
        }
        
        SysUser newUser = new SysUser();
        newUser.setUsername(dto.getPhone());
        newUser.setPassword(BCrypt.hashpw(dto.getPassword()));
        newUser.setRealName("商户[" + dto.getPhone() + "]");
        newUser.setRole("merchant");
        
        sysUserMapper.insert(newUser);
        return Result.success("注册成功！");
    }
}
