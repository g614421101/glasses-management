package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import com.glasses.constant.RoleConstants;
import com.glasses.dto.ChangePasswordDTO;
import com.glasses.dto.LoginDTO;
import com.glasses.dto.ProfileUpdateDTO;
import com.glasses.dto.RegisterDTO;
import com.glasses.dto.SetupDTO;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.AuthService;
import com.glasses.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 登录防爆破：同一账号连续失败 N 次后临时锁定（内存计数，重启失效） */
    private static final int LOGIN_MAX_FAILURES = 5;
    private static final long LOGIN_LOCK_MILLIS = 15 * 60 * 1000L;
    private static final Map<String, LoginFailState> LOGIN_FAILURES = new ConcurrentHashMap<>();

    private static final class LoginFailState {
        volatile int count;
        volatile long lockedUntil;
    }

    @Value("${app.invite-code}")
    private String inviteCode;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        if (StrUtil.isBlank(loginDTO.getUsername()) || StrUtil.isBlank(loginDTO.getPassword())) {
            throw new BizException("请输入账号和密码");
        }

        String account = loginDTO.getUsername().trim();
        LoginFailState failState = LOGIN_FAILURES.get(account);
        if (failState != null && failState.lockedUntil > System.currentTimeMillis()) {
            log.warn("账号 {} 处于临时锁定中，拒绝登录", account);
            throw new BizException("失败次数过多，请15分钟后再试");
        }
        if (!isInitialized()) {
            throw new BizException("系统尚未初始化，请先完成管理员初始化");
        }

        SysUser user = sysUserMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(SysUser.class)
                        .where(SysUser::getUsername).eq(account)
                        .or(SysUser::getPhone).eq(account));

        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFailure(account);
            log.info("用户登录失败: {} (用户名或密码错误)", account);
            throw new BizException("用户名或密码错误");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            log.info("用户登录失败: {} (账号已删除)", account);
            throw new BizException("该账号已删除，请联系超管恢复");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            log.info("用户登录失败: {} (账号已封禁)", account);
            throw new BizException("该账号已封禁，请联系超管解除封禁");
        }

        StpUtil.login(user.getId());
        refreshSession(user);
        LOGIN_FAILURES.remove(account);
        log.info("用户登录成功: {} (id={})", account, user.getId());

        Map<String, Object> data = buildUserInfo(user);
        data.put("token", StpUtil.getTokenValue());
        return data;
    }

    private void recordLoginFailure(String account) {
        LoginFailState state = LOGIN_FAILURES.computeIfAbsent(account, k -> new LoginFailState());
        synchronized (state) {
            state.count++;
            if (state.count >= LOGIN_MAX_FAILURES) {
                state.lockedUntil = System.currentTimeMillis() + LOGIN_LOCK_MILLIS;
                state.count = 0;
                log.warn("账号 {} 连续登录失败，临时锁定 {} 分钟", account, LOGIN_LOCK_MILLIS / 60000);
            }
        }
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!isInitialized()) {
            throw new BizException("系统尚未初始化，请先完成管理员初始化");
        }
        if (!inviteCode.equals(dto.getInviteCode())) {
            throw new BizException("邀请码不正确");
        }
        if (StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPhone())
                || StrUtil.isBlank(dto.getPassword()) || StrUtil.isBlank(dto.getConfirmPassword())) {
            throw new BizException("请完整填写注册信息");
        }
        String username = dto.getUsername().trim();
        String phone = dto.getPhone().trim();
        if (username.length() < 3 || username.length() > 30) {
            throw new BizException("用户名长度需为 3-30 位");
        }
        if (!PhoneUtil.isMobile(phone)) {
            throw new BizException("手机号格式不正确");
        }
        if (dto.getPassword().length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }

        Long duplicateCount = sysUserMapper.countByUsernameOrPhoneIncludingDeleted(username, phone);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException("用户名或手机号已被注册");
        }

        SysUser newUser = new SysUser();
        newUser.setUsername(username);
        newUser.setPhone(phone);
        newUser.setPassword(BCrypt.hashpw(dto.getPassword()));
        newUser.setRealName("商户[" + username + "]");
        newUser.setRole(RoleConstants.MERCHANT);
        newUser.setMustChangePassword(false);
        newUser.setDisabled(false);
        newUser.setDeleted(false);
        sysUserMapper.insert(newUser);
        log.info("新用户注册: {} (phone={})", username, phone);
    }

    @Override
    public boolean isInitialized() {
        return sysUserMapper.selectAnyByRole(RoleConstants.ADMIN) != null;
    }

    @Override
    public void setupAdmin(SetupDTO dto) {
        if (isInitialized()) {
            throw new BizException("系统已初始化，无需重复操作");
        }
        if (dto == null || StrUtil.isBlank(dto.getInviteCode())
                || !inviteCode.equals(dto.getInviteCode().trim())) {
            throw new BizException("邀请码不正确");
        }
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        if (username.length() < 3 || username.length() > 30) {
            throw new BizException("用户名长度需为 3-30 位");
        }
        if (StrUtil.isBlank(dto.getPassword()) || dto.getPassword().length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }

        Long duplicateCount = sysUserMapper.countByUsernameOrPhoneIncludingDeleted(username, username);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException("用户名已被使用");
        }

        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPassword(BCrypt.hashpw(dto.getPassword()));
        admin.setRealName("管理员[" + username + "]");
        admin.setRole(RoleConstants.ADMIN);
        admin.setMustChangePassword(true);
        admin.setDisabled(false);
        admin.setDeleted(false);
        sysUserMapper.insert(admin);
        log.warn("系统初始化完成: 管理员账号 {} 已创建", username);
    }

    @Override
    public Map<String, Object> getInfo() {
        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new BizException("该账号已封禁");
        }
        refreshSession(user);
        return buildUserInfo(user);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        if (StrUtil.isBlank(dto.getOldPassword()) || StrUtil.isBlank(dto.getNewPassword()) || StrUtil.isBlank(dto.getConfirmPassword())) {
            throw new BizException("请完整填写密码信息");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException("两次输入的新密码不一致");
        }

        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("用户不存在");
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("当前密码不正确");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        user.setMustChangePassword(false);
        sysUserMapper.update(user, true);
        refreshSession(user);
        log.info("修改密码: userId={}", user.getId());
    }

    @Override
    public Map<String, Object> updateProfile(ProfileUpdateDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPhone())) {
            throw new BizException("请完整填写用户名和手机号");
        }

        String username = dto.getUsername().trim();
        String phone = dto.getPhone().trim();
        String realName = StrUtil.blankToDefault(dto.getRealName(), "").trim();
        if (username.length() < 3 || username.length() > 30) {
            throw new BizException("用户名长度需要为 3-30 位");
        }
        if (!PhoneUtil.isMobile(phone)) {
            throw new BizException("手机号格式不正确");
        }
        if (realName.length() > 50) {
            throw new BizException("显示名称不能超过 50 个字符");
        }

        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new BizException("该账号已封禁");
        }

        Long duplicateCount = sysUserMapper.countByUsernameOrPhoneExcludingIdIncludingDeleted(user.getId(), username, phone);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException("用户名或手机号已被使用");
        }

        user.setUsername(username);
        user.setPhone(phone);
        user.setRealName(StrUtil.isBlank(realName) ? username : realName);
        sysUserMapper.update(user, true);
        refreshSession(user);
        log.info("修改个人资料: userId={}", user.getId());
        return buildUserInfo(user);
    }

    private SysUser currentUser() {
        return sysUserMapper.selectOneById(StpUtil.getLoginIdAsLong());
    }

    private void refreshSession(SysUser user) {
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());
        StpUtil.getSession().set("role", user.getRole());
    }

    private Map<String, Object> buildUserInfo(SysUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("createTime", user.getCreateTime());
        data.put("mustChangePassword", Boolean.TRUE.equals(user.getMustChangePassword()));
        data.put("disabled", Boolean.TRUE.equals(user.getDisabled()));
        data.put("deleted", Boolean.TRUE.equals(user.getDeleted()));
        return data;
    }
}
