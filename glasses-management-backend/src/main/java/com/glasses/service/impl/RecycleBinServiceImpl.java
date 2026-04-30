package com.glasses.service.impl;

import cn.hutool.core.date.DateUtil;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import com.glasses.mapper.CustomerMapper;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.RecycleBinService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    private static final int RETENTION_DAYS = 30;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> list(String type) {
        Map<String, Object> result = new HashMap<>();
        if ("all".equalsIgnoreCase(type) || "customer".equalsIgnoreCase(type)) {
            result.put("customers", customerMapper.selectDeletedList());
        }
        if ("all".equalsIgnoreCase(type) || "optometry".equalsIgnoreCase(type)) {
            result.put("optometryRecords", optometryRecordMapper.selectDeletedList());
        }
        if ("all".equalsIgnoreCase(type) || "sales".equalsIgnoreCase(type)) {
            result.put("salesRecords", salesRecordMapper.selectDeletedList());
        }
        return result;
    }

    @Override
    @Transactional
    public Result<Boolean> restore(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectAnyById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("回收站中未找到该顾客");
            }
            customerMapper.restoreByIdIgnoringLogic(id);
            restoreRecordsByCustomer(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该验光记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            optometryRecordMapper.restoreByIdIgnoringLogic(id);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该配镜记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            salesRecordMapper.restoreByIdIgnoringLogic(id);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Result<Boolean> purge(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectAnyById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("只能彻底删除回收站中的顾客");
            }
            purgeRecordsByCustomer(id);
            customerMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的验光记录");
            }
            optometryRecordMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的配镜记录");
            }
            salesRecordMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Map<String, Integer> purgeExpired() {
        Date expireBefore = DateUtil.offsetDay(new Date(), -RETENTION_DAYS);
        Map<String, Integer> result = new HashMap<>();

        int sales = salesRecordMapper.physicalDeleteExpired(expireBefore);
        int optometry = optometryRecordMapper.physicalDeleteExpired(expireBefore);
        int customers = customerMapper.physicalDeleteExpired(expireBefore);
        int users = sysUserMapper.physicalDeleteExpired(RoleConstants.ADMIN, expireBefore);

        result.put("sales", sales);
        result.put("optometry", optometry);
        result.put("customers", customers);
        result.put("users", users);
        return result;
    }

    private boolean isCustomerDeleted(Long customerId) {
        Customer customer = customerMapper.selectAnyById(customerId);
        return customer == null || Boolean.TRUE.equals(customer.getDeleted());
    }

    private void restoreRecordsByCustomer(Long customerId) {
        List<OptometryRecord> optometryRecords = optometryRecordMapper.selectDeletedByCustomerId(customerId);
        for (OptometryRecord record : optometryRecords) {
            optometryRecordMapper.restoreByIdIgnoringLogic(record.getId());
        }

        List<SalesRecord> salesRecords = salesRecordMapper.selectDeletedByCustomerId(customerId);
        for (SalesRecord record : salesRecords) {
            salesRecordMapper.restoreByIdIgnoringLogic(record.getId());
        }
    }

    private void purgeRecordsByCustomer(Long customerId) {
        optometryRecordMapper.physicalDeleteByCustomerId(customerId);
        salesRecordMapper.physicalDeleteByCustomerId(customerId);
    }
}
