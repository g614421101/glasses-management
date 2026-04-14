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

/**
 * 应用启动时初始化超管账号。
 * 无论数据库里的密码哈希是否合法，都确保 admin 账号存在且密码始终为 REMOVED_ADMIN_PASSWORD。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "REMOVED_ADMIN_PASSWORD";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void run(ApplicationArguments args) {
        SysUser admin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, ADMIN_USERNAME)
        );

        if (admin == null) {
            // 超管不存在，全新创建
            SysUser newAdmin = new SysUser();
            newAdmin.setUsername(ADMIN_USERNAME);
            newAdmin.setPassword(BCrypt.hashpw(ADMIN_PASSWORD));
            newAdmin.setRealName("系统管理员");
            newAdmin.setRole("admin");
            sysUserMapper.insert(newAdmin);
            log.info("[DataInitializer] 超管账号 admin 创建成功，密码已通过 BCrypt 加密写入。");
        } else {
            // 超管已存在，强制刷新密码哈希（确保哈希值合法有效）
            admin.setPassword(BCrypt.hashpw(ADMIN_PASSWORD));
            sysUserMapper.updateById(admin);
            log.info("[DataInitializer] 超管账号 admin 密码哈希已刷新校验完毕。");
        }
    }
}
