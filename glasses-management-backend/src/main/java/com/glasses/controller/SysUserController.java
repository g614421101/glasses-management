package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/sys-user")
@SaCheckRole(RoleConstants.ADMIN)
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<List<SysUser>> listUsers(@RequestParam(defaultValue = "false") Boolean includeDeleted) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getRole, RoleConstants.ADMIN);
        if (!Boolean.TRUE.equals(includeDeleted)) {
            wrapper.eq(SysUser::getDeleted, false);
        }
        wrapper.orderByDesc(SysUser::getDeleted)
                .orderByDesc(SysUser::getCreateTime);

        List<SysUser> users = sysUserMapper.selectList(wrapper);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PostMapping("/disable/{id}")
    public Result<Boolean> disableUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("已删除账号不能封禁");
        }
        user.setDisabled(true);
        user.setDisabledTime(new Date());
        sysUserMapper.updateById(user);
        return Result.success(true);
    }

    @PostMapping("/enable/{id}")
    public Result<Boolean> enableUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        user.setDisabled(false);
        user.setDisabledTime(null);
        sysUserMapper.updateById(user);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        user.setDeleted(true);
        user.setDeletedTime(new Date());
        user.setDisabled(true);
        user.setDisabledTime(new Date());
        sysUserMapper.updateById(user);
        return Result.success(true);
    }

    @PostMapping("/restore/{id}")
    public Result<Boolean> restoreUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        user.setDeleted(false);
        user.setDeletedTime(null);
        user.setDisabled(false);
        user.setDisabledTime(null);
        sysUserMapper.updateById(user);
        return Result.success(true);
    }

    @DeleteMapping("/purge/{id}")
    public Result<Boolean> purgeUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        if (!Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("只能彻底删除已进入回收站的商户账号");
        }
        sysUserMapper.deleteById(id);
        return Result.success(true);
    }

    @PostMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("已删除账号不能重置密码，请先恢复");
        }
        user.setPassword(BCrypt.hashpw("REMOVED_RESET_PASSWORD"));
        user.setMustChangePassword(true);
        sysUserMapper.updateById(user);
        return Result.success("密码已重置为：REMOVED_RESET_PASSWORD");
    }

    private SysUser findMerchant(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || RoleConstants.ADMIN.equals(user.getRole())) {
            return null;
        }
        return user;
    }
}
