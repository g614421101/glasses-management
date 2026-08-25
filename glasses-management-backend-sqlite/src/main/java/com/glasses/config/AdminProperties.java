package com.glasses.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "glasses.admin")
public class AdminProperties {

    @NotBlank(message = "管理员用户名不能为空")
    private String username = "admin";

    /**
     * 管理员初始明文密码，仅首次创建账号时使用（可选）。
     * 与 password-hash 二选一；两者都为空时不自动创建，等待 Web 端初始化引导。
     */
    private String password;

    /**
     * 管理员初始密码的 BCrypt 哈希（可选），与 password 二选一，避免明文落盘。
     */
    private String passwordHash;

    private String realName = "系统管理员";
    private boolean enabled = true;

    /**
     * 一次性强制重置开关：true 时启动会把管理员密码重置为随机强密码
     * （仅打印一次到日志），并记录 force_reset_time 防止重复执行。
     */
    private boolean forceResetPassword = false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isForceResetPassword() {
        return forceResetPassword;
    }

    public void setForceResetPassword(boolean forceResetPassword) {
        this.forceResetPassword = forceResetPassword;
    }
}
