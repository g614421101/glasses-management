package com.glasses.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.Customer;

public interface CustomerService extends IService<Customer> {
    Page<Customer> searchCustomer(String keyword, Integer current, Integer size);
}
