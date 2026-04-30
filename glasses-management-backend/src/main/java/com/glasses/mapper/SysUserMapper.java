package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser selectAnyById(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUser selectAnyByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE role = #{role} ORDER BY id LIMIT 1")
    SysUser selectAnyByRole(@Param("role") String role);

    @Select("""
            <script>
            SELECT * FROM sys_user
            WHERE role &lt;&gt; #{adminRole}
            <if test="includeDeleted == false">
                AND deleted = false
            </if>
            ORDER BY deleted DESC, create_time DESC
            </script>
            """)
    List<SysUser> selectMerchants(@Param("includeDeleted") boolean includeDeleted,
                                  @Param("adminRole") String adminRole);

    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} OR phone = #{phone}")
    Long countByUsernameOrPhoneIncludingDeleted(@Param("username") String username,
                                                @Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM sys_user WHERE id <> #{id} AND (username = #{username} OR phone = #{phone})")
    Long countByUsernameOrPhoneExcludingIdIncludingDeleted(@Param("id") Long id,
                                                           @Param("username") String username,
                                                           @Param("phone") String phone);

    @Update("""
            UPDATE sys_user
            SET role = #{role},
                real_name = #{realName},
                deleted = false,
                deleted_time = NULL,
                disabled = false,
                disabled_time = NULL
            WHERE id = #{id}
            """)
    int syncAdminAccount(@Param("id") Long id,
                         @Param("role") String role,
                         @Param("realName") String realName);

    @Update("""
            UPDATE sys_user
            SET deleted = true,
                deleted_time = #{deletedTime},
                disabled = true,
                disabled_time = #{deletedTime}
            WHERE id = #{id}
              AND role <> #{adminRole}
              AND deleted = false
            """)
    int softDeleteMerchantById(@Param("id") Long id,
                               @Param("adminRole") String adminRole,
                               @Param("deletedTime") Date deletedTime);

    @Update("UPDATE sys_user SET deleted = false, deleted_time = NULL, disabled = false, disabled_time = NULL WHERE id = #{id} AND role <> #{adminRole}")
    int restoreMerchantById(@Param("id") Long id, @Param("adminRole") String adminRole);

    @Delete("DELETE FROM sys_user WHERE id = #{id} AND role <> #{adminRole} AND deleted = true")
    int physicalDeleteMerchantById(@Param("id") Long id, @Param("adminRole") String adminRole);

    @Delete("DELETE FROM sys_user WHERE deleted = true AND role <> #{adminRole} AND deleted_time < #{expireBefore}")
    int physicalDeleteExpired(@Param("adminRole") String adminRole, @Param("expireBefore") Date expireBefore);
}
