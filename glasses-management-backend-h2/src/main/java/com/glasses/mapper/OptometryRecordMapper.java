package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.OptometryRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface OptometryRecordMapper extends BaseMapper<OptometryRecord> {
    @Select("SELECT * FROM optometry_record WHERE id = #{id}")
    OptometryRecord selectAnyById(@Param("id") Long id);

    @Select("SELECT * FROM optometry_record WHERE deleted = true ORDER BY deleted_time DESC, id DESC")
    List<OptometryRecord> selectDeletedList();

    @Select("SELECT * FROM optometry_record WHERE customer_id = #{customerId} AND deleted = true")
    List<OptometryRecord> selectDeletedByCustomerId(@Param("customerId") Long customerId);

    @Update("UPDATE optometry_record SET deleted = true, deleted_time = #{deletedTime}, deleted_by = #{deletedBy} WHERE id = #{id} AND deleted = false")
    int softDeleteById(@Param("id") Long id,
                       @Param("deletedTime") Date deletedTime,
                       @Param("deletedBy") Long deletedBy);

    @Update("UPDATE optometry_record SET deleted = true, deleted_time = #{deletedTime}, deleted_by = #{deletedBy} WHERE customer_id = #{customerId} AND deleted = false")
    int softDeleteByCustomerId(@Param("customerId") Long customerId,
                               @Param("deletedTime") Date deletedTime,
                               @Param("deletedBy") Long deletedBy);

    @Update("UPDATE optometry_record SET deleted = false, deleted_time = NULL, deleted_by = NULL WHERE id = #{id}")
    int restoreByIdIgnoringLogic(@Param("id") Long id);

    @Delete("DELETE FROM optometry_record WHERE id = #{id} AND deleted = true")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM optometry_record WHERE customer_id = #{customerId} AND deleted = true")
    int physicalDeleteByCustomerId(@Param("customerId") Long customerId);

    @Delete("DELETE FROM optometry_record WHERE deleted = true AND deleted_time < #{expireBefore}")
    int physicalDeleteExpired(@Param("expireBefore") Date expireBefore);

    @Select("SELECT * FROM optometry_record")
    List<OptometryRecord> selectAllIncludingDeleted();

    @Select("SELECT * FROM optometry_record WHERE customer_id = #{customerId} AND exam_date >= #{examDateStart} AND exam_date < #{examDateEnd}")
    List<OptometryRecord> findByCustomerAndExamDate(@Param("customerId") Long customerId,
                                                     @Param("examDateStart") Date examDateStart,
                                                     @Param("examDateEnd") Date examDateEnd);

    @Delete("DELETE FROM optometry_record")
    int deleteAll();
}
