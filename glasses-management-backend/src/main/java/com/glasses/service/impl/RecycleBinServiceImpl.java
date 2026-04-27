package com.glasses.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
            result.put("customers", customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getDeleted, true)
                    .orderByDesc(Customer::getDeletedTime)
                    .orderByDesc(Customer::getId)));
        }
        if ("all".equalsIgnoreCase(type) || "optometry".equalsIgnoreCase(type)) {
            result.put("optometryRecords", optometryRecordMapper.selectList(new LambdaQueryWrapper<OptometryRecord>()
                    .eq(OptometryRecord::getDeleted, true)
                    .orderByDesc(OptometryRecord::getDeletedTime)
                    .orderByDesc(OptometryRecord::getId)));
        }
        if ("all".equalsIgnoreCase(type) || "sales".equalsIgnoreCase(type)) {
            result.put("salesRecords", salesRecordMapper.selectList(new LambdaQueryWrapper<SalesRecord>()
                    .eq(SalesRecord::getDeleted, true)
                    .orderByDesc(SalesRecord::getDeletedTime)
                    .orderByDesc(SalesRecord::getId)));
        }
        return result;
    }

    @Override
    @Transactional
    public Result<Boolean> restore(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("回收站中未找到该顾客");
            }
            customer.setDeleted(false);
            customer.setDeletedTime(null);
            customer.setDeletedBy(null);
            customerMapper.updateById(customer);
            restoreRecordsByCustomer(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该验光记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            record.setDeleted(false);
            record.setDeletedTime(null);
            record.setDeletedBy(null);
            optometryRecordMapper.updateById(record);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该配镜记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            record.setDeleted(false);
            record.setDeletedTime(null);
            record.setDeletedBy(null);
            salesRecordMapper.updateById(record);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Result<Boolean> purge(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("只能彻底删除回收站中的顾客");
            }
            purgeRecordsByCustomer(id);
            customerMapper.deleteById(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的验光记录");
            }
            optometryRecordMapper.deleteById(id);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的配镜记录");
            }
            salesRecordMapper.deleteById(id);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Map<String, Integer> purgeExpired() {
        Date expireBefore = DateUtil.offsetDay(new Date(), -RETENTION_DAYS);
        Map<String, Integer> result = new HashMap<>();

        int sales = salesRecordMapper.delete(new LambdaQueryWrapper<SalesRecord>()
                .eq(SalesRecord::getDeleted, true)
                .lt(SalesRecord::getDeletedTime, expireBefore));
        int optometry = optometryRecordMapper.delete(new LambdaQueryWrapper<OptometryRecord>()
                .eq(OptometryRecord::getDeleted, true)
                .lt(OptometryRecord::getDeletedTime, expireBefore));
        int customers = customerMapper.delete(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getDeleted, true)
                .lt(Customer::getDeletedTime, expireBefore));
        int users = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, true)
                .ne(SysUser::getRole, RoleConstants.ADMIN)
                .lt(SysUser::getDeletedTime, expireBefore));

        result.put("sales", sales);
        result.put("optometry", optometry);
        result.put("customers", customers);
        result.put("users", users);
        return result;
    }

    private boolean isCustomerDeleted(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        return customer == null || Boolean.TRUE.equals(customer.getDeleted());
    }

    private void restoreRecordsByCustomer(Long customerId) {
        List<OptometryRecord> optometryRecords = optometryRecordMapper.selectList(new LambdaQueryWrapper<OptometryRecord>()
                .eq(OptometryRecord::getCustomerId, customerId)
                .eq(OptometryRecord::getDeleted, true));
        for (OptometryRecord record : optometryRecords) {
            record.setDeleted(false);
            record.setDeletedTime(null);
            record.setDeletedBy(null);
            optometryRecordMapper.updateById(record);
        }

        List<SalesRecord> salesRecords = salesRecordMapper.selectList(new LambdaQueryWrapper<SalesRecord>()
                .eq(SalesRecord::getCustomerId, customerId)
                .eq(SalesRecord::getDeleted, true));
        for (SalesRecord record : salesRecords) {
            record.setDeleted(false);
            record.setDeletedTime(null);
            record.setDeletedBy(null);
            salesRecordMapper.updateById(record);
        }
    }

    private void purgeRecordsByCustomer(Long customerId) {
        optometryRecordMapper.delete(new QueryWrapper<OptometryRecord>().eq("customer_id", customerId).eq("deleted", true));
        salesRecordMapper.delete(new QueryWrapper<SalesRecord>().eq("customer_id", customerId).eq("deleted", true));
    }
}
