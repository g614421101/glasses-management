package com.glasses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.OptometryRecord;

import java.util.List;

public interface OptometryRecordService extends IService<OptometryRecord> {
    List<OptometryRecord> listByCustomerId(Long customerId);
}
