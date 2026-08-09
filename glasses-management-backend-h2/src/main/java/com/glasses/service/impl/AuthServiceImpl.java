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

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

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
        SysUser user = sysUserMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(SysUser.class)
                        .where(SysUser::getUsername).eq(account)
                        .or(SysUser::getPhone).eq(account));

        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
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
        log.info("用户登录成功: {} (id={})", account, user.getId());

        Map<String, Object> data = buildUserInfo(user);
        data.put("token", StpUtil.getTokenValue());
        return data;
    }

    @Override
    public void register(RegisterDTO dto) {
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
