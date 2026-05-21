package com.glasses.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.SalesRecord;

import java.util.List;

public interface SalesRecordService extends IService<SalesRecord> {
    List<SalesRecord> listByCustomerId(Long customerId);

    /**
     * 分页查询某顾客的配镜记录
     */
    Page<SalesRecord> listByCustomerId(Long customerId, Integer current, Integer size);

    /**
     * 软删除配镜记录（含存在性检查）
     * @return true 表示删除成功，false 表示记录不存在
     */
    boolean softDeleteRecord(Long id);

    /**
     * 营收汇总统计（SQL 聚合查询）
     */
    java.util.Map<String, Object> getRevenueSummary(String startDate, String endDate);
}