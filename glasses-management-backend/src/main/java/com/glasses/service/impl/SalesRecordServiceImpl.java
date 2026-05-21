package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
                .eq(SalesRecord::getDeleted, false)
                .orderByDesc(SalesRecord::getSalesDate));
    }

    @Override
    public Page<SalesRecord> listByCustomerId(Long customerId, Integer current, Integer size) {
        Page<SalesRecord> page = new Page<>(current, size);
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SalesRecord>()
                .eq(SalesRecord::getCustomerId, customerId)
                .eq(SalesRecord::getDeleted, false)
                .orderByDesc(SalesRecord::getSalesDate));
    }

    @Override
    public boolean softDeleteRecord(Long id) {
        SalesRecord record = baseMapper.selectAnyById(id);
        if (record == null) {
            return false;
        }
        return baseMapper.softDeleteById(id, DateUtil.date(), StpUtil.getLoginIdAsLong()) > 0;
    }

    @Override
    public java.util.Map<String, Object> getRevenueSummary(String startDate, String endDate) {
        String start = (startDate != null && !startDate.isEmpty()) ? startDate + " 00:00:00" : null;
        String end = (endDate != null && !endDate.isEmpty()) ? endDate + " 23:59:59" : null;
        return baseMapper.selectRevenueSummary(start, end);
    }
}