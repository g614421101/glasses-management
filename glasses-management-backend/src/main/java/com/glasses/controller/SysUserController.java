package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys-user")
@SaCheckRole("admin") // 仅允许超管访问整个控制器
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<List<SysUser>> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(
            new LambdaQueryWrapper<SysUser>().ne(SysUser::getRole, "admin")
            .orderByDesc(SysUser::getCreateTime)
        );
        // 不暴露密码哈希给前端
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if(user != null && "admin".equals(user.getRole())) {
            return Result.error("禁止删除管理员账号");
        }
        sysUserMapper.deleteById(id);
        return Result.success("账号已删除");
    }

    @PostMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if(user == null) {
            return Result.error("账号不存在");
        }
        if("admin".equals(user.getRole())) {
            return Result.error("禁止重置管理员密码");
        }
        // 重置为 REMOVED_RESET_PASSWORD
        user.setPassword(BCrypt.hashpw("REMOVED_RESET_PASSWORD"));
        sysUserMapper.updateById(user);
        return Result.success("密码已重置为：REMOVED_RESET_PASSWORD");
    }
}
