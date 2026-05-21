package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glasses.entity.Customer;
import com.glasses.mapper.CustomerMapper;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Override
    public Page<Customer> searchCustomer(String keyword, Integer current, Integer size) {
        Page<Customer> page = new Page<>(current, size);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getDeleted, false);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Customer::getPhone, keyword)
                    .or()
                    .like(Customer::getName, keyword));
        }
        wrapper.orderByDesc(Customer::getCreateTime)
               .orderByDesc(Customer::getId);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean softDeleteCustomer(Long id) {
        Customer customer = baseMapper.selectAnyById(id);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return false;
        }
        Date now = DateUtil.date();
        Long loginId = StpUtil.getLoginIdAsLong();
        baseMapper.softDeleteById(id, now, loginId);
        optometryRecordMapper.softDeleteByCustomerId(id, now, loginId);
        salesRecordMapper.softDeleteByCustomerId(id, now, loginId);
        return true;
    }
}
