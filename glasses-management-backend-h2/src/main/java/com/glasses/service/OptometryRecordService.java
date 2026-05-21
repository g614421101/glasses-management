package com.glasses.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.OptometryRecord;

import java.util.List;

public interface OptometryRecordService extends IService<OptometryRecord> {
    List<OptometryRecord> listByCustomerId(Long customerId);

    /**
     * 分页查询某顾客的验光记录
     */
    Page<OptometryRecord> listByCustomerId(Long customerId, Integer current, Integer size);

    /**
     * 软删除验光记录（含存在性检查）
     * @return true 表示删除成功，false 表示记录不存在
     */
    boolean softDeleteRecord(Long id);
}