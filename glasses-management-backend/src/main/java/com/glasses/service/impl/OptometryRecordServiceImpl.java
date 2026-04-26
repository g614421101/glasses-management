package com.glasses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glasses.entity.OptometryRecord;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.service.OptometryRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptometryRecordServiceImpl extends ServiceImpl<OptometryRecordMapper, OptometryRecord> implements OptometryRecordService {

    @Override
    public List<OptometryRecord> listByCustomerId(Long customerId) {
        return baseMapper.selectList(new LambdaQueryWrapper<OptometryRecord>()
                .eq(OptometryRecord::getCustomerId, customerId)
                .orderByDesc(OptometryRecord::getExamDate));
    }
}
