package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
                .eq(OptometryRecord::getDeleted, false)
                .orderByDesc(OptometryRecord::getExamDate));
    }

    @Override
    public Page<OptometryRecord> listByCustomerId(Long customerId, Integer current, Integer size) {
        Page<OptometryRecord> page = new Page<>(current, size);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<OptometryRecord>()
                .eq(OptometryRecord::getCustomerId, customerId)
                .eq(OptometryRecord::getDeleted, false)
                .orderByDesc(OptometryRecord::getExamDate));
    }

    @Override
    public boolean softDeleteRecord(Long id) {
        OptometryRecord record = baseMapper.selectAnyById(id);
        if (record == null) {
            return false;
        }
        return baseMapper.softDeleteById(id, DateUtil.date(), StpUtil.getLoginIdAsLong()) > 0;
    }
}