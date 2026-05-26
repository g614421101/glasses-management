package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sys-user")
@SaCheckRole(RoleConstants.ADMIN)
public class SysUserController {

    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final char[] RESET_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
    private static final int RESET_PASSWORD_LENGTH = 12;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<List<SysUser>> listUsers(@RequestParam(defaultValue = "false") Boolean includeDeleted) {
        List<SysUser> users = sysUserMapper.selectMerchants(Boolean.TRUE.equals(includeDeleted), RoleConstants.ADMIN);
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
        StpUtil.logout(user.getId());
        log.info("封禁商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
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
        log.info("解封商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        sysUserMapper.softDeleteMerchantById(id, RoleConstants.ADMIN, new Date());
        StpUtil.logout(user.getId());
        log.info("删除商户(软): id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
        return Result.success(true);
    }

    @PostMapping("/restore/{id}")
    public Result<Boolean> restoreUser(@PathVariable Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            return Result.error("账号不存在或不允许操作");
        }
        sysUserMapper.restoreMerchantById(id, RoleConstants.ADMIN);
        log.info("恢复商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
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
        sysUserMapper.physicalDeleteMerchantById(id, RoleConstants.ADMIN);
        log.warn("彻底删除商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
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

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(BCrypt.hashpw(temporaryPassword));
        user.setMustChangePassword(true);
        sysUserMapper.updateById(user);
        log.info("重置商户密码: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
        return Result.success("临时密码：" + temporaryPassword);
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(RESET_PASSWORD_LENGTH);
        for (int i = 0; i < RESET_PASSWORD_LENGTH; i++) {
            password.append(RESET_PASSWORD_CHARS[PASSWORD_RANDOM.nextInt(RESET_PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }

    private SysUser findMerchant(Long id) {
        SysUser user = sysUserMapper.selectAnyById(id);
        if (user == null || RoleConstants.ADMIN.equals(user.getRole())) {
            return null;
        }
        return user;
    }
}
