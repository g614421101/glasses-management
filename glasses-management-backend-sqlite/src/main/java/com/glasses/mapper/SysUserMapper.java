package com.glasses.mapper;

import com.mybatisflex.core.BaseMapper;
import com.glasses.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectAnyById(@Param("id") Long id);

    SysUser selectAnyByUsername(@Param("username") String username);

    SysUser selectAnyByRole(@Param("role") String role);

    List<SysUser> selectMerchants(@Param("includeDeleted") boolean includeDeleted,
                                  @Param("adminRole") String adminRole);

    Long countByUsernameOrPhoneIncludingDeleted(@Param("username") String username,
                                                @Param("phone") String phone);

    Long countByUsernameOrPhoneExcludingIdIncludingDeleted(@Param("id") Long id,
                                                           @Param("username") String username,
                                                           @Param("phone") String phone);

    int syncAdminAccount(@Param("id") Long id,
                         @Param("role") String role,
                         @Param("realName") String realName);

    int softDeleteMerchantById(@Param("id") Long id,
                               @Param("adminRole") String adminRole,
                               @Param("deletedTime") Date deletedTime);

    int restoreMerchantById(@Param("id") Long id, @Param("adminRole") String adminRole);

    int physicalDeleteMerchantById(@Param("id") Long id, @Param("adminRole") String adminRole);

    int physicalDeleteExpired(@Param("adminRole") String adminRole, @Param("expireBefore") Date expireBefore);

    List<SysUser> selectAllIncludingDeleted();

    int deleteAllNonAdmin(@Param("adminRole") String adminRole);
}
