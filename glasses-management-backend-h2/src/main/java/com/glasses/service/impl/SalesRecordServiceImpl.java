package com.glasses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glasses.entity.SalesRecord;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.SalesRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesRecordServiceImpl extends ServiceImpl<SalesRecordMapper, SalesRecord> implements SalesRecordService {

    @Override
    public List<SalesRecord> listByCustomerId(Long customerId) {
        return baseMapper.selectList(new LambdaQueryWrapper<SalesRecord>()
                .eq(SalesRecord::getCustomerId, customerId)
                .orderByDesc(SalesRecord::getSalesDate));
    }
}
