package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.OptometryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface OptometryRecordMapper extends BaseMapper<OptometryRecord> {

    OptometryRecord selectAnyById(@Param("id") Long id);

    List<OptometryRecord> selectDeletedList();

    List<OptometryRecord> selectDeletedByCustomerId(@Param("customerId") Long customerId);

    int softDeleteById(@Param("id") Long id,
                       @Param("deletedTime") Date deletedTime,
                       @Param("deletedBy") Long deletedBy);

    int softDeleteByCustomerId(@Param("customerId") Long customerId,
                               @Param("deletedTime") Date deletedTime,
                               @Param("deletedBy") Long deletedBy);

    int restoreByIdIgnoringLogic(@Param("id") Long id);

    int restoreByCustomerIdIgnoringLogic(@Param("customerId") Long customerId);

    int physicalDeleteById(@Param("id") Long id);

    int physicalDeleteByCustomerId(@Param("customerId") Long customerId);

    int physicalDeleteExpired(@Param("expireBefore") Date expireBefore);

    List<OptometryRecord> selectAllIncludingDeleted();

    List<OptometryRecord> findByCustomerAndExamDate(@Param("customerId") Long customerId,
                                                    @Param("examDateStart") Date examDateStart,
                                                    @Param("examDateEnd") Date examDateEnd);

    int deleteAll();
}
