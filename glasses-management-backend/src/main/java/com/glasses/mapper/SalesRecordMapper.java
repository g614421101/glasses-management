package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.SalesRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface SalesRecordMapper extends BaseMapper<SalesRecord> {
    @Select("SELECT * FROM sales_record WHERE id = #{id}")
    SalesRecord selectAnyById(@Param("id") Long id);

    @Select("SELECT * FROM sales_record WHERE deleted = true ORDER BY deleted_time DESC, id DESC")
    List<SalesRecord> selectDeletedList();

    @Select("SELECT * FROM sales_record WHERE customer_id = #{customerId} AND deleted = true")
    List<SalesRecord> selectDeletedByCustomerId(@Param("customerId") Long customerId);

    @Update("UPDATE sales_record SET deleted = true, deleted_time = #{deletedTime}, deleted_by = #{deletedBy} WHERE id = #{id} AND deleted = false")
    int softDeleteById(@Param("id") Long id,
                       @Param("deletedTime") Date deletedTime,
                       @Param("deletedBy") Long deletedBy);

    @Update("UPDATE sales_record SET deleted = true, deleted_time = #{deletedTime}, deleted_by = #{deletedBy} WHERE customer_id = #{customerId} AND deleted = false")
    int softDeleteByCustomerId(@Param("customerId") Long customerId,
                               @Param("deletedTime") Date deletedTime,
                               @Param("deletedBy") Long deletedBy);

    @Update("UPDATE sales_record SET deleted = false, deleted_time = NULL, deleted_by = NULL WHERE id = #{id}")
    int restoreByIdIgnoringLogic(@Param("id") Long id);

    @Delete("DELETE FROM sales_record WHERE id = #{id} AND deleted = true")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM sales_record WHERE customer_id = #{customerId} AND deleted = true")
    int physicalDeleteByCustomerId(@Param("customerId") Long customerId);

    @Delete("DELETE FROM sales_record WHERE deleted = true AND deleted_time < #{expireBefore}")
    int physicalDeleteExpired(@Param("expireBefore") Date expireBefore);

    @Select("SELECT * FROM sales_record")
    List<SalesRecord> selectAllIncludingDeleted();

    @Select("SELECT * FROM sales_record WHERE record_no = #{recordNo} LIMIT 1")
    SalesRecord selectByRecordNoIncludingDeleted(@Param("recordNo") String recordNo);

    @Delete("DELETE FROM sales_record")
    int deleteAll();
}
