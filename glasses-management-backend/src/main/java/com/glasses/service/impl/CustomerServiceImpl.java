package com.glasses.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glasses.entity.Customer;
import com.glasses.mapper.CustomerMapper;
import com.glasses.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Override
    public Page<Customer> searchCustomer(String keyword, Integer current, Integer size) {
        Page<Customer> page = new Page<>(current, size);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Customer::getPhone, keyword)
                    .or()
                    .like(Customer::getName, keyword));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
