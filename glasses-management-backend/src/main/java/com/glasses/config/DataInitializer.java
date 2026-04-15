package com.glasses.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AdminProperties adminProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!adminProperties.isEnabled()) {
            log.info("[DataInitializer] 超管自动初始化已禁用");
            return;
        }

        SysUser admin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, adminProperties.getUsername())
        );

        if (admin == null) {
            SysUser newAdmin = new SysUser();
            newAdmin.setUsername(adminProperties.getUsername());
            newAdmin.setPassword(BCrypt.hashpw(adminProperties.getPassword()));
            newAdmin.setRealName(adminProperties.getRealName());
            newAdmin.setRole("admin");
            sysUserMapper.insert(newAdmin);
            log.info("[DataInitializer] 超管账号 {} 创建成功", adminProperties.getUsername());
        } else {
            // 调试点：同步幂等逻辑，仅在配置发生变化时才执行数据库更新
            boolean passwordMatch = BCrypt.checkpw(adminProperties.getPassword(), admin.getPassword());
            boolean realNameMatch = adminProperties.getRealName().equals(admin.getRealName());

            if (!passwordMatch || !realNameMatch) {
                admin.setPassword(BCrypt.hashpw(adminProperties.getPassword()));
                admin.setRealName(adminProperties.getRealName());
                sysUserMapper.updateById(admin);
                log.info("[DataInitializer] 超管账号 {} 配置已更新（映射到 MySQL）", adminProperties.getUsername());
            } else {
                log.info("[DataInitializer] 超管账号 {} 配置已同步（MySQL 数据已是最新）", adminProperties.getUsername());
            }
        }
    }
}