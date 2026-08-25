package com.glasses;

import cn.hutool.crypto.digest.BCrypt;
import com.glasses.config.AdminProperties;
import com.glasses.config.DataInitializer;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataInitializer 统一预设策略的单元测试。
 * 不依赖 Spring 上下文，SysUserMapper 用动态代理模拟一张 sys_user 表。
 */
public class DataInitializerTest {

    /** 模拟 sys_user 表 */
    private final List<SysUser> userTable = new ArrayList<>();

    @BeforeEach
    public void clearTable() {
        userTable.clear();
    }

    @Test
    public void testBothPasswordAndHashRejected() throws Exception {
        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setPassword("plain");
        props.setPasswordHash("hash");
        props.setEnabled(true);

        DataInitializer initializer = newInitializer(props);
        assertThrows(IllegalStateException.class,
                () -> initializer.run(new DefaultApplicationArguments(new String[0])),
                "password 与 password-hash 同时设置必须启动失败");
        assertEquals(0, userTable.size(), "不应创建任何账号");
    }

    @Test
    public void testCreateWithPlainPassword() throws Exception {
        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setPassword("Secret123!");
        props.setRealName("系统管理员");
        props.setEnabled(true);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        assertEquals(1, userTable.size());
        SysUser admin = userTable.get(0);
        assertEquals("admin", admin.getUsername());
        assertEquals("admin", admin.getRole());
        assertEquals("系统管理员", admin.getRealName());
        assertTrue(Boolean.TRUE.equals(admin.getMustChangePassword()), "首次创建应要求改密");
        assertTrue(BCrypt.checkpw("Secret123!", admin.getPassword()), "配置的明文密码应可登录");
    }

    @Test
    public void testCreateWithPasswordHash() throws Exception {
        String hash = BCrypt.hashpw("HashPwd!23");
        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setPasswordHash(hash);
        props.setEnabled(true);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        assertEquals(1, userTable.size());
        assertEquals(hash, userTable.get(0).getPassword(), "配置的哈希应原样写入");
        assertTrue(BCrypt.checkpw("HashPwd!23", userTable.get(0).getPassword()));
    }

    @Test
    public void testNoConfigDoesNotCreate() throws Exception {
        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setEnabled(true);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        assertEquals(0, userTable.size(), "无密码配置且无 admin 时应留给 Web 引导，不自动创建");
    }

    @Test
    public void testDisabledSkips() throws Exception {
        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setPassword("Secret123!");
        props.setEnabled(false);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        assertEquals(0, userTable.size());
    }

    @Test
    public void testExistingAdminPasswordNeverOverwritten() throws Exception {
        seedAdmin("admin", BCrypt.hashpw("OldPwd123"), false, false);

        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setPassword("NewPwd456");
        props.setRealName("系统管理员");
        props.setEnabled(true);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        SysUser admin = userTable.get(0);
        assertTrue(BCrypt.checkpw("OldPwd123", admin.getPassword()), "已存在的 admin 密码不应被配置覆盖");
        assertFalse(BCrypt.checkpw("NewPwd456", admin.getPassword()));
    }

    @Test
    public void testForceResetOnce() throws Exception {
        seedAdmin("admin", BCrypt.hashpw("OldPwd123"), false, false);

        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setRealName("系统管理员");
        props.setEnabled(true);
        props.setForceResetPassword(true);

        DataInitializer initializer = newInitializer(props);
        initializer.run(new DefaultApplicationArguments(new String[0]));

        SysUser admin = userTable.get(0);
        assertFalse(BCrypt.checkpw("OldPwd123", admin.getPassword()), "强制重置后旧密码应失效");
        assertTrue(Boolean.TRUE.equals(admin.getMustChangePassword()));
        assertNotNull(admin.getForceResetTime(), "应记录重置时间防止重复执行");
        String firstHash = admin.getPassword();

        // 第二次启动：force_reset_time 已非空，不再重置
        initializer.run(new DefaultApplicationArguments(new String[0]));
        assertEquals(firstHash, userTable.get(0).getPassword(), "force-reset 只能执行一次");
    }

    @Test
    public void testForceResetFlagOffDoesNotTouch() throws Exception {
        seedAdmin("admin", BCrypt.hashpw("OldPwd123"), false, false);

        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setRealName("系统管理员");
        props.setEnabled(true);
        props.setForceResetPassword(false);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        assertTrue(BCrypt.checkpw("OldPwd123", userTable.get(0).getPassword()));
        assertNull(userTable.get(0).getForceResetTime());
    }

    @Test
    public void testSelfHealDeletedDisabledAndRole() throws Exception {
        SysUser admin = seedAdmin("admin", BCrypt.hashpw("OldPwd123"), true, true);
        admin.setRole("merchant");
        admin.setRealName("旧名字");
        admin.setDeletedTime(new Date());
        admin.setDisabledTime(new Date());

        AdminProperties props = new AdminProperties();
        props.setUsername("admin");
        props.setRealName("系统管理员");
        props.setEnabled(true);

        newInitializer(props).run(new DefaultApplicationArguments(new String[0]));

        SysUser healed = userTable.get(0);
        assertEquals("admin", healed.getRole(), "角色应自愈为 admin");
        assertEquals("系统管理员", healed.getRealName());
        assertFalse(Boolean.TRUE.equals(healed.getDeleted()));
        assertFalse(Boolean.TRUE.equals(healed.getDisabled()));
        assertTrue(BCrypt.checkpw("OldPwd123", healed.getPassword()), "自愈不应动密码");
    }

    // ── 工具方法 ────────────────────────────────────────────────

    private SysUser seedAdmin(String username, String passwordHash, boolean deleted, boolean disabled) {
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername(username);
        admin.setPassword(passwordHash);
        admin.setRealName("系统管理员");
        admin.setRole("admin");
        admin.setMustChangePassword(false);
        admin.setDeleted(deleted);
        admin.setDisabled(disabled);
        userTable.add(admin);
        return admin;
    }

    private DataInitializer newInitializer(AdminProperties props) throws Exception {
        DataInitializer initializer = new DataInitializer();
        setField(initializer, "sysUserMapper", fakeMapper());
        setField(initializer, "adminProperties", props);
        return initializer;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = DataInitializer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private SysUserMapper fakeMapper() {
        return (SysUserMapper) Proxy.newProxyInstance(
                SysUserMapper.class.getClassLoader(),
                new Class<?>[]{SysUserMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "selectAnyByRole": {
                            String role = (String) args[0];
                            return userTable.stream()
                                    .filter(u -> role.equals(u.getRole()))
                                    .findFirst().orElse(null);
                        }
                        case "selectAnyByUsername": {
                            String username = (String) args[0];
                            return userTable.stream()
                                    .filter(u -> username.equals(u.getUsername()))
                                    .findFirst().orElse(null);
                        }
                        case "insert": {
                            SysUser copy = copyOf((SysUser) args[0]);
                            copy.setId((long) (userTable.size() + 1));
                            userTable.add(copy);
                            return 1;
                        }
                        case "update": {
                            SysUser src = (SysUser) args[0];
                            SysUser target = userTable.stream()
                                    .filter(u -> u.getId() != null && u.getId().equals(src.getId()))
                                    .findFirst().orElse(null);
                            if (target != null) {
                                merge(target, src);
                            }
                            return 1;
                        }
                        case "syncAdminAccount": {
                            Long id = (Long) args[0];
                            String role = (String) args[1];
                            String realName = (String) args[2];
                            userTable.stream().filter(u -> id.equals(u.getId())).findFirst()
                                    .ifPresent(u -> {
                                        u.setRole(role);
                                        u.setRealName(realName);
                                        u.setDeleted(false);
                                        u.setDeletedTime(null);
                                        u.setDisabled(false);
                                        u.setDisabledTime(null);
                                    });
                            return 1;
                        }
                        case "countByUsernameOrPhoneIncludingDeleted":
                        case "countByUsernameOrPhoneExcludingIdIncludingDeleted":
                            return 0L;
                        default:
                            Class<?> returnType = method.getReturnType();
                            if (returnType == int.class) {
                                return 0;
                            }
                            if (returnType == long.class) {
                                return 0L;
                            }
                            if (returnType == boolean.class) {
                                return false;
                            }
                            return null;
                    }
                });
    }

    private SysUser copyOf(SysUser src) {
        SysUser copy = new SysUser();
        copy.setId(src.getId());
        copy.setUsername(src.getUsername());
        copy.setPhone(src.getPhone());
        copy.setPassword(src.getPassword());
        copy.setRealName(src.getRealName());
        copy.setRole(src.getRole());
        copy.setMustChangePassword(src.getMustChangePassword());
        copy.setDisabled(src.getDisabled());
        copy.setDisabledTime(src.getDisabledTime());
        copy.setDeleted(src.getDeleted());
        copy.setDeletedTime(src.getDeletedTime());
        copy.setForceResetTime(src.getForceResetTime());
        return copy;
    }

    private void merge(SysUser target, SysUser src) {
        if (src.getUsername() != null) {
            target.setUsername(src.getUsername());
        }
        if (src.getPhone() != null) {
            target.setPhone(src.getPhone());
        }
        if (src.getPassword() != null) {
            target.setPassword(src.getPassword());
        }
        if (src.getRealName() != null) {
            target.setRealName(src.getRealName());
        }
        if (src.getRole() != null) {
            target.setRole(src.getRole());
        }
        if (src.getMustChangePassword() != null) {
            target.setMustChangePassword(src.getMustChangePassword());
        }
        if (src.getDisabled() != null) {
            target.setDisabled(src.getDisabled());
        }
        if (src.getDisabledTime() != null) {
            target.setDisabledTime(src.getDisabledTime());
        }
        if (src.getDeleted() != null) {
            target.setDeleted(src.getDeleted());
        }
        if (src.getDeletedTime() != null) {
            target.setDeletedTime(src.getDeletedTime());
        }
        if (src.getForceResetTime() != null) {
            target.setForceResetTime(src.getForceResetTime());
        }
    }
}
