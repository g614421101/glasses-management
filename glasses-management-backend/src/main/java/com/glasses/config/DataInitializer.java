package com.glasses.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Date;

/**
 * 管理员账号统一预设策略（所有部署形态一致）：
 * <ol>
 *   <li>创建时机：仅当 sys_user 中不存在 admin 角色时创建。</li>
 *   <li>初始密码来源：glasses.admin.password（明文，仅首建生效）
 *       或 glasses.admin.password-hash（BCrypt 哈希，避免明文落盘），两者不能同时设置；
 *       都为空时不自动创建，登录页通过 /api/system/setup 引导完成初始化。</li>
 *   <li>密码只写一次：账号已存在时任何启动都不覆盖密码。</li>
 *   <li>状态自愈：每次启动幂等修复 admin 的 role/realName/deleted/disabled。</li>
 *   <li>强制重置：glasses.admin.force-reset-password=true 且 force_reset_time 为空时，
 *       重置为随机强密码（仅打印一次到日志），并记录时间防止重复执行。</li>
 * </ol>
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final char[] PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
    private static final int PASSWORD_LENGTH = 12;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AdminProperties adminProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!adminProperties.isEnabled()) {
            log.info("[DataInitializer] admin initialization disabled");
            return;
        }

        boolean hasPassword = StrUtil.isNotBlank(adminProperties.getPassword());
        boolean hasHash = StrUtil.isNotBlank(adminProperties.getPasswordHash());
        if (hasPassword && hasHash) {
            throw new IllegalStateException(
                    "glasses.admin.password 与 glasses.admin.password-hash 只能配置一个");
        }

        SysUser admin = sysUserMapper.selectAnyByRole(RoleConstants.ADMIN);

        SysUser configuredUser = sysUserMapper.selectAnyByUsername(adminProperties.getUsername());
        if (admin == null && configuredUser != null) {
            admin = configuredUser;
        }

        if (admin == null) {
            if (!hasPassword && !hasHash) {
                log.info("[DataInitializer] 未配置管理员初始密码，跳过自动创建，等待 Web 端初始化引导");
                return;
            }
            String passwordHash = hasHash ? adminProperties.getPasswordHash()
                    : BCrypt.hashpw(adminProperties.getPassword());
            SysUser newAdmin = new SysUser();
            newAdmin.setUsername(adminProperties.getUsername());
            newAdmin.setPassword(passwordHash);
            newAdmin.setRealName(adminProperties.getRealName());
            newAdmin.setRole(RoleConstants.ADMIN);
            newAdmin.setMustChangePassword(true);
            newAdmin.setDisabled(false);
            newAdmin.setDeleted(false);
            sysUserMapper.insert(newAdmin);
            log.info("[DataInitializer] admin account {} created", adminProperties.getUsername());
            return;
        }

        // 一次性强制重置：仅当开关打开且从未执行过（force_reset_time 为空）
        if (adminProperties.isForceResetPassword() && admin.getForceResetTime() == null) {
            String newPassword = generateRandomPassword();
            admin.setPassword(BCrypt.hashpw(newPassword));
            admin.setMustChangePassword(true);
            admin.setForceResetTime(new Date());
            sysUserMapper.update(admin, true);
            log.warn("[DataInitializer] 管理员密码已强制重置，新密码（仅显示一次）: {}", newPassword);
        }

        boolean changed = false;
        if (!RoleConstants.ADMIN.equals(admin.getRole())) {
            admin.setRole(RoleConstants.ADMIN);
            changed = true;
        }
        if (!adminProperties.getRealName().equals(admin.getRealName())) {
            admin.setRealName(adminProperties.getRealName());
            changed = true;
        }
        if (Boolean.TRUE.equals(admin.getDeleted())) {
            admin.setDeleted(false);
            admin.setDeletedTime(null);
            changed = true;
        }
        if (Boolean.TRUE.equals(admin.getDisabled())) {
            admin.setDisabled(false);
            admin.setDisabledTime(null);
            changed = true;
        }

        if (changed) {
            sysUserMapper.syncAdminAccount(admin.getId(), RoleConstants.ADMIN, adminProperties.getRealName());
            log.info("[DataInitializer] admin account {} status synchronized", admin.getUsername());
        } else {
            log.info("[DataInitializer] admin account {} exists; password is not overwritten", admin.getUsername());
        }
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS[PASSWORD_RANDOM.nextInt(PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }
}
