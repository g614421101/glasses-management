package com.glasses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.SalesRecord;

import java.util.List;

public interface SalesRecordService extends IService<SalesRecord> {
    List<SalesRecord> listByCustomerId(Long customerId);
}
