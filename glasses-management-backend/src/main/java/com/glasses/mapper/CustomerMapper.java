package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.Customer;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
    @Select("SELECT * FROM customer WHERE id = #{id}")
    Customer selectAnyById(@Param("id") Long id);

    @Select("SELECT * FROM customer WHERE deleted = true ORDER BY deleted_time DESC, id DESC")
    List<Customer> selectDeletedList();

    @Update("UPDATE customer SET deleted = true, deleted_time = #{deletedTime}, deleted_by = #{deletedBy} WHERE id = #{id} AND deleted = false")
    int softDeleteById(@Param("id") Long id,
                       @Param("deletedTime") Date deletedTime,
                       @Param("deletedBy") Long deletedBy);

    @Update("UPDATE customer SET deleted = false, deleted_time = NULL, deleted_by = NULL WHERE id = #{id}")
    int restoreByIdIgnoringLogic(@Param("id") Long id);

    @Delete("DELETE FROM customer WHERE id = #{id} AND deleted = true")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM customer WHERE deleted = true AND deleted_time < #{expireBefore}")
    int physicalDeleteExpired(@Param("expireBefore") Date expireBefore);

    @Select("SELECT * FROM customer")
    List<Customer> selectAllIncludingDeleted();

    @Select("SELECT * FROM customer WHERE phone = #{phone} LIMIT 1")
    Customer selectByPhoneIncludingDeleted(@Param("phone") String phone);

    @Delete("DELETE FROM customer")
    int deleteAll();
}
