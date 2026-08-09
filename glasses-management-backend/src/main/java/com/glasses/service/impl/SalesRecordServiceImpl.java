package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.glasses.entity.SalesRecord;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.SalesRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SalesRecordServiceImpl extends ServiceImpl<SalesRecordMapper, SalesRecord> implements SalesRecordService {

    @Override
    public List<SalesRecord> listByCustomerId(Long customerId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .from(SalesRecord.class)
                        .where(SalesRecord::getCustomerId).eq(customerId)
                        .and(SalesRecord::getDeleted).eq(false)
                        .orderBy(SalesRecord::getSalesDate).desc());
    }

    @Override
    public Page<SalesRecord> listByCustomerId(Long customerId, Integer current, Integer size) {
        Page<SalesRecord> page = Page.of(current, size);
        return mapper.paginate(page,
                QueryWrapper.create()
                        .from(SalesRecord.class)
                        .where(SalesRecord::getCustomerId).eq(customerId)
                        .and(SalesRecord::getDeleted).eq(false)
                        .orderBy(SalesRecord::getSalesDate).desc());
    }

    @Override
    public boolean softDeleteRecord(Long id) {
        SalesRecord record = mapper.selectAnyById(id);
        if (record == null) {
            return false;
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        boolean result = mapper.softDeleteById(id, DateUtil.date(), loginId) > 0;
        if (result) {
            log.info("软删除配镜记录: id={}, 操作人={}", id, loginId);
        }
        return result;
    }

    @Override
    public java.util.Map<String, Object> getRevenueSummary(String startDate, String endDate) {
        String start = (startDate != null && !startDate.isEmpty()) ? startDate + " 00:00:00" : null;
        String end = (endDate != null && !endDate.isEmpty()) ? endDate + " 23:59:59" : null;
        return mapper.selectRevenueSummary(start, end);
    }
}
