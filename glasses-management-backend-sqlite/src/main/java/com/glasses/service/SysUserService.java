package com.glasses.service;

import com.glasses.entity.SysUser;

import java.util.List;

/**
 * 商户账号管理服务：封禁/解封、软删/恢复/彻底删除、重置密码。
 */
public interface SysUserService {

    /**
     * 查询商户账号列表（admin 不返回）。
     *
     * @param includeDeleted 是否包含回收站中的账号
     */
    List<SysUser> listUsers(Boolean includeDeleted);

    /**
     * 封禁商户账号（同时强制下线）。
     */
    void disableUser(Long id);

    /**
     * 解封商户账号。
     */
    void enableUser(Long id);

    /**
     * 软删除商户账号（移入回收站并强制下线）。
     */
    void deleteUser(Long id);

    /**
     * 从回收站恢复商户账号。
     */
    void restoreUser(Long id);

    /**
     * 彻底删除回收站中的商户账号。
     */
    void purgeUser(Long id);

    /**
     * 重置商户账号密码。
     *
     * @return 生成的临时密码
     */
    String resetPassword(Long id);
}
