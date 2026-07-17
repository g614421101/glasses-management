package com.glasses.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glasses.entity.SalesRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesRecordMapper extends BaseMapper<SalesRecord> {

    SalesRecord selectAnyById(@Param("id") Long id);

    List<SalesRecord> selectDeletedList();

    List<SalesRecord> selectDeletedByCustomerId(@Param("customerId") Long customerId);

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

    List<SalesRecord> selectAllIncludingDeleted();

    SalesRecord selectByRecordNoIncludingDeleted(@Param("recordNo") String recordNo);

    Map<String, Object> selectRevenueSummary(@Param("startDate") String startDate, @Param("endDate") String endDate);

    int deleteAll();
}
