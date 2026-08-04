package com.glasses.service;

import com.glasses.dto.ChangePasswordDTO;
import com.glasses.dto.LoginDTO;
import com.glasses.dto.ProfileUpdateDTO;
import com.glasses.dto.RegisterDTO;

import java.util.Map;

/**
 * 认证与用户账号服务：登录、注册、个人资料、修改密码。
 */
public interface AuthService {

    /**
     * 登录：校验账号密码与账号状态，建立 Sa-Token 会话。
     *
     * @return 用户信息（含 token）
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 注册商户账号。
     */
    void register(RegisterDTO dto);

    /**
     * 当前登录用户信息。
     */
    Map<String, Object> getInfo();

    /**
     * 修改当前用户密码。
     */
    void changePassword(ChangePasswordDTO dto);

    /**
     * 更新当前用户个人资料。
     *
     * @return 更新后的用户信息
     */
    Map<String, Object> updateProfile(ProfileUpdateDTO dto);
}
