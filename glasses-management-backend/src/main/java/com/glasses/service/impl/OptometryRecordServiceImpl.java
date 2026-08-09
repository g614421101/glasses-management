package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.glasses.entity.OptometryRecord;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.service.OptometryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OptometryRecordServiceImpl extends ServiceImpl<OptometryRecordMapper, OptometryRecord> implements OptometryRecordService {

    @Override
    public List<OptometryRecord> listByCustomerId(Long customerId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .from(OptometryRecord.class)
                        .where(OptometryRecord::getCustomerId).eq(customerId)
                        .and(OptometryRecord::getDeleted).eq(false)
                        .orderBy(OptometryRecord::getExamDate).desc());
    }

    @Override
    public Page<OptometryRecord> listByCustomerId(Long customerId, Integer current, Integer size) {
        Page<OptometryRecord> page = Page.of(current, size);
        return mapper.paginate(page,
                QueryWrapper.create()
                        .from(OptometryRecord.class)
                        .where(OptometryRecord::getCustomerId).eq(customerId)
                        .and(OptometryRecord::getDeleted).eq(false)
                        .orderBy(OptometryRecord::getExamDate).desc());
    }

    @Override
    public boolean softDeleteRecord(Long id) {
        OptometryRecord record = mapper.selectAnyById(id);
        if (record == null) {
            return false;
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        boolean result = mapper.softDeleteById(id, DateUtil.date(), loginId) > 0;
        if (result) {
            log.info("软删除验光记录: id={}, 操作人={}", id, loginId);
        }
        return result;
    }
}
