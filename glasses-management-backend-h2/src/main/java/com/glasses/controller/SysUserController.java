package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.service.SysUserService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys-user")
@SaCheckRole(RoleConstants.ADMIN)
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list")
    public Result<List<SysUser>> listUsers(@RequestParam(defaultValue = "false") Boolean includeDeleted) {
        return Result.success(sysUserService.listUsers(includeDeleted));
    }

    @PostMapping("/disable/{id}")
    public Result<Boolean> disableUser(@PathVariable Long id) {
        sysUserService.disableUser(id);
        return Result.success(true);
    }

    @PostMapping("/enable/{id}")
    public Result<Boolean> enableUser(@PathVariable Long id) {
        sysUserService.enableUser(id);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success(true);
    }

    @PostMapping("/restore/{id}")
    public Result<Boolean> restoreUser(@PathVariable Long id) {
        sysUserService.restoreUser(id);
        return Result.success(true);
    }

    @DeleteMapping("/purge/{id}")
    public Result<Boolean> purgeUser(@PathVariable Long id) {
        sysUserService.purgeUser(id);
        return Result.success(true);
    }

    @PostMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        return Result.success("临时密码：" + sysUserService.resetPassword(id));
    }
}
