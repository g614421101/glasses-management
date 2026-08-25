package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.SysUserService;
import com.glasses.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final char[] RESET_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
    private static final int RESET_PASSWORD_LENGTH = 12;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysUser> listUsers(Boolean includeDeleted) {
        List<SysUser> users = sysUserMapper.selectMerchants(Boolean.TRUE.equals(includeDeleted), RoleConstants.ADMIN);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public void disableUser(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("已删除账号不能封禁");
        }
        user.setDisabled(true);
        user.setDisabledTime(new Date());
        sysUserMapper.update(user, true);
        StpUtil.logout(user.getId());
        log.info("封禁商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
    }

    @Override
    public void enableUser(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        user.setDisabled(false);
        user.setDisabledTime(null);
        sysUserMapper.update(user, true);
        log.info("解封商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        sysUserMapper.softDeleteMerchantById(id, RoleConstants.ADMIN, new Date());
        StpUtil.logout(user.getId());
        log.info("删除商户(软): id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
    }

    @Override
    public void restoreUser(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        sysUserMapper.restoreMerchantById(id, RoleConstants.ADMIN);
        log.info("恢复商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
    }

    @Override
    public void purgeUser(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        if (!Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("只能彻底删除已进入回收站的商户账号");
        }
        sysUserMapper.physicalDeleteMerchantById(id, RoleConstants.ADMIN);
        log.warn("彻底删除商户: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
    }

    @Override
    public String resetPassword(Long id) {
        SysUser user = findMerchant(id);
        if (user == null) {
            throw new BizException("账号不存在或不允许操作");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("已删除账号不能重置密码，请先恢复");
        }

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(BCrypt.hashpw(temporaryPassword));
        user.setMustChangePassword(true);
        sysUserMapper.update(user, true);
        log.info("重置商户密码: id={}, username={}, 操作人={}", id, user.getUsername(), StpUtil.getLoginIdAsLong());
        return temporaryPassword;
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
