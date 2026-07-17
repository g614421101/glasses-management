package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    Customer selectAnyById(@Param("id") Long id);

    List<Customer> selectDeletedList();

    int softDeleteById(@Param("id") Long id,
                       @Param("deletedTime") Date deletedTime,
                       @Param("deletedBy") Long deletedBy);

    int restoreByIdIgnoringLogic(@Param("id") Long id);

    int physicalDeleteById(@Param("id") Long id);

    int physicalDeleteExpired(@Param("expireBefore") Date expireBefore);

    List<Customer> selectAllIncludingDeleted();

    Customer selectByPhoneIncludingDeleted(@Param("phone") String phone);

    int deleteAll();
}
