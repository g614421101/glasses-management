package com.glasses.config;

import cn.hutool.crypto.digest.BCrypt;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

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

        SysUser admin = sysUserMapper.selectAnyByRole(RoleConstants.ADMIN);

        SysUser configuredUser = sysUserMapper.selectAnyByUsername(adminProperties.getUsername());
        if (admin == null && configuredUser != null) {
            admin = configuredUser;
        }

        if (admin == null) {
            SysUser newAdmin = new SysUser();
            newAdmin.setUsername(adminProperties.getUsername());
            newAdmin.setPassword(BCrypt.hashpw(adminProperties.getPassword()));
            newAdmin.setRealName(adminProperties.getRealName());
            newAdmin.setRole(RoleConstants.ADMIN);
            newAdmin.setMustChangePassword(true);
            newAdmin.setDisabled(false);
            newAdmin.setDeleted(false);
            sysUserMapper.insert(newAdmin);
            log.info("[DataInitializer] admin account {} created", adminProperties.getUsername());
            return;
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
}
