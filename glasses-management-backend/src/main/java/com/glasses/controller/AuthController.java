package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.glasses.constant.RoleConstants;
import com.glasses.dto.ChangePasswordDTO;
import com.glasses.dto.LoginDTO;
import com.glasses.dto.ProfileUpdateDTO;
import com.glasses.dto.RegisterDTO;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @org.springframework.beans.factory.annotation.Value("${app.invite-code}")
    private String inviteCode;

    @Autowired
    private SysUserMapper sysUserMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        if (StrUtil.isBlank(loginDTO.getUsername()) || StrUtil.isBlank(loginDTO.getPassword())) {
            return Result.error("请输入账号和密码");
        }

        String account = loginDTO.getUsername().trim();
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getPhone, account))
                .last("LIMIT 1"));

        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        if (Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("该账号已删除，请联系超管恢复");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            return Result.error("该账号已封禁，请联系超管解除封禁");
        }

        StpUtil.login(user.getId());
        refreshSession(user);

        Map<String, Object> data = buildUserInfo(user);
        data.put("token", StpUtil.getTokenValue());
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        if (!inviteCode.equals(dto.getInviteCode())) {
            return Result.error("邀请码不正确");
        }
        if (StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPhone())
                || StrUtil.isBlank(dto.getPassword()) || StrUtil.isBlank(dto.getConfirmPassword())) {
            return Result.error("请完整填写注册信息");
        }
        String username = dto.getUsername().trim();
        String phone = dto.getPhone().trim();
        if (username.length() < 3 || username.length() > 30) {
            return Result.error("用户名长度需为 3-30 位");
        }
        if (!PhoneUtil.isMobile(phone)) {
            return Result.error("手机号格式不正确");
        }
        if (dto.getPassword().length() < 6) {
            return Result.error("密码至少 6 位");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        Long duplicateCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .or()
                .eq(SysUser::getPhone, phone));
        if (duplicateCount != null && duplicateCount > 0) {
            return Result.error("用户名或手机号已被注册");
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
        return Result.success("注册成功");
    }

    @SaCheckLogin
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo() {
        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            return Result.error("该账号已封禁");
        }
        refreshSession(user);
        return Result.success(buildUserInfo(user));
    }

    @SaCheckLogin
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody ChangePasswordDTO dto) {
        if (StrUtil.isBlank(dto.getOldPassword()) || StrUtil.isBlank(dto.getNewPassword()) || StrUtil.isBlank(dto.getConfirmPassword())) {
            return Result.error("请完整填写密码信息");
        }
        if (dto.getNewPassword().length() < 6) {
            return Result.error("新密码至少 6 位");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("用户不存在");
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            return Result.error("当前密码不正确");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        user.setMustChangePassword(false);
        sysUserMapper.updateById(user);
        refreshSession(user);
        return Result.success(true);
    }

    @SaCheckLogin
    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPhone())) {
            return Result.error("请完整填写用户名和手机号");
        }

        String username = dto.getUsername().trim();
        String phone = dto.getPhone().trim();
        String realName = StrUtil.blankToDefault(dto.getRealName(), "").trim();
        if (username.length() < 3 || username.length() > 30) {
            return Result.error("用户名长度需要为 3-30 位");
        }
        if (!PhoneUtil.isMobile(phone)) {
            return Result.error("手机号格式不正确");
        }
        if (realName.length() > 50) {
            return Result.error("显示名称不能超过 50 个字符");
        }

        SysUser user = currentUser();
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            return Result.error("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getDisabled())) {
            return Result.error("该账号已封禁");
        }

        Long duplicateCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getId, user.getId())
                .and(w -> w.eq(SysUser::getUsername, username).or().eq(SysUser::getPhone, phone)));
        if (duplicateCount != null && duplicateCount > 0) {
            return Result.error("用户名或手机号已被使用");
        }

        user.setUsername(username);
        user.setPhone(phone);
        user.setRealName(StrUtil.isBlank(realName) ? username : realName);
        sysUserMapper.updateById(user);
        refreshSession(user);
        return Result.success(buildUserInfo(user));
    }

    private SysUser currentUser() {
        return sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
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
